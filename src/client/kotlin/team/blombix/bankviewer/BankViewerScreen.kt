package team.blombix.bankviewer

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import kotlin.math.ceil
import kotlin.math.max

class BankViewerScreen : Screen(Text.literal("Bank Viewer")) {

    private val mc = MinecraftClient.getInstance()

    private var selectedPlayer: String? = null
    private var selectedBankType: String? = null
    private var selectedPage: Int? = null
    private var searchQuery: String = ""

    private var scrollOffset = 0.0
    private var maxScroll = 0.0
    private var hoveredStack: ItemStack? = null

    private lateinit var searchBox: TextFieldWidget

    override fun init() {
        super.init()
        clearChildren()

        // === 🔵 Pasek wyszukiwania (50% szerokości, wyśrodkowany) ===
        val searchWidth = width / 2
        val searchX = width / 2 - searchWidth / 2
        searchBox = TextFieldWidget(textRenderer, searchX, 10, searchWidth, 20, Text.literal("Search"))
        searchBox.setPlaceholder(Text.literal("Search items..."))
        searchBox.setChangedListener {
            searchQuery = it.lowercase()
        }
        addDrawableChild(searchBox)

        // === 🟧 Pomarańczowa sekcja (lewa góra): wybór stron ===
        var leftY = 40
        val leftX = 10
        addDrawableChild(ButtonWidget.builder(Text.literal("Aggregate")) {
            selectedBankType = null
            selectedPage = null
            init()
        }.dimensions(leftX, leftY, 100, 20).build())

        leftY += 24
        for (bankType in gatherBankTypes()) {
            addDrawableChild(ButtonWidget.builder(Text.literal(bankType)) {
                selectedBankType = bankType
                selectedPage = null
                init()
            }.dimensions(leftX, leftY, 100, 20).build())
            leftY += 24

            val playerForPages = selectedPlayer ?: continue
            val pages = BankStorageManager.getPages(playerForPages, bankType).keys.sorted()
            for (p in pages) {
                addDrawableChild(ButtonWidget.builder(Text.literal("Page $p")) {
                    selectedBankType = bankType
                    selectedPage = p
                    init()
                }.dimensions(leftX + 10, leftY, 80, 18).build())
                leftY += 20
            }
            leftY += 8
        }

        // === 🟨 Żółta sekcja — pusta (pod pomarańczową) ===
        leftY += 20
        addDrawableChild(
            ButtonWidget.builder(Text.literal("(Empty Zone)")) {}.dimensions(leftX, leftY, 100, 20).build()
        )

        // === 🟪 Fioletowa sekcja (po prawej): lista kont ===
        var rightY = 40
        val rightX = width - 120
        val players = BankStorageManager.getPlayers().sorted()
        for (p in players) {
            addDrawableChild(ButtonWidget.builder(Text.literal(p)) {
                selectedPlayer = p
                init()
            }.dimensions(rightX, rightY, 100, 20).build())
            rightY += 24
        }

        // === 🟫 Przycisk Close (dół po prawej) ===
        addDrawableChild(ButtonWidget.builder(Text.literal("Close")) {
            mc.setScreen(null)
        }.dimensions(width - 110, height - 30, 100, 20).build())
    }

    private fun gatherBankTypes(): List<String> {
        val types = mutableSetOf<String>()
        val players = if (selectedPlayer == null) BankStorageManager.getPlayers() else listOf(selectedPlayer!!)
        for (p in players) {
            types.addAll(BankStorageManager.getBanksForPlayer(p).keys)
        }
        return types.sorted()
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)

        // === Tło sekcji (szare półprzezroczyste) ===
        context.fill(0, 35, 120, height - 40, 0x44000000.toInt()) // lewa sekcja (pomarańczowa + żółta)
        context.fill(width - 130, 35, width - 10, height - 40, 0x44000000.toInt()) // prawa (fioletowa)
        context.fill(width / 4, 40, width - width / 4, height - 50, 0x22000000.toInt()) // środkowa przestrzeń (itemy)
        context.fill(width / 4, 5, width - width / 4, 35, 0x33000000.toInt()) // tło paska wyszukiwania

        // === Tytuł ===
        val title = Text.literal("Bank Viewer")
        context.drawText(textRenderer, title, width / 2 - textRenderer.getWidth(title) / 2, 6, 0xFFFFFF, false)

        // === Pasek wyszukiwania ===
        searchBox.render(context, mouseX, mouseY, delta)

        // === Rysowanie elementów banku ===
        val x = width / 4 + 20
        val y = 60
        val w = width / 2
        val h = height - 120

        hoveredStack = null

        when {
            selectedBankType == null -> renderAggregate(context, x, y, w, h, mouseX, mouseY)
            selectedPage == null -> renderOverview(context, x, y, w, h)
            else -> renderPage(context, x, y, w, h, mouseX, mouseY)
        }

        // === Tooltipy ===
        hoveredStack?.let { stack ->
            try {
                val tooltip = stack.getTooltip(
                    net.minecraft.item.Item.TooltipContext.DEFAULT,
                    mc.player,
                    net.minecraft.item.tooltip.TooltipType.BASIC
                )
                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY)
            } catch (_: Throwable) {
                context.drawTooltip(textRenderer, listOf(Text.literal(stack.name.string)), mouseX, mouseY)
            }
        }


        super.render(context, mouseX, mouseY, delta)
    }

    private fun renderBackground(context: DrawContext) {}

    private fun renderAggregate(context: DrawContext, x: Int, y: Int, w: Int, h: Int, mouseX: Int, mouseY: Int) {
        val agg = BankStorageManager.getAggregateFor(selectedPlayer, null)
        context.drawText(textRenderer, Text.literal("Aggregate summary:"), x, y - 12, 0xFFFFFF, false)

        val filtered = if (searchQuery.isNotBlank()) {
            agg.filter { it.key.lowercase().contains(searchQuery) }
        } else agg

        val cols = 8
        val slot = 20
        val rows = ceil(filtered.size / cols.toDouble()).toInt()
        maxScroll = max(0.0, rows * slot - h + 20.0)

        var idx = 0
        for ((_, rec) in filtered.entries) {
            val stack = BankStorageManager.itemRecordToItemStack(rec)
            val row = idx / cols
            val col = idx % cols
            val sx = x + col * slot
            val sy = (y + row * slot - scrollOffset).toInt()
            if (sy in (y - slot)..(y + h)) {
                context.drawItem(stack, sx, sy)
                context.drawItemInSlot(textRenderer, stack, sx, sy)
                if (mouseX in sx..(sx + 16) && mouseY in sy..(sy + 16)) hoveredStack = stack
            }
            idx++
        }
    }

    private fun renderOverview(context: DrawContext, x: Int, y: Int, w: Int, h: Int) {
        val bank = selectedBankType ?: return
        context.drawText(textRenderer, Text.literal("$bank - Overview"), x, y - 12, 0xFFFFFF, false)
        var yy = y
        val players = if (selectedPlayer == null) BankStorageManager.getPlayers() else listOf(selectedPlayer!!)
        for (p in players) {
            val pages = BankStorageManager.getBanksForPlayer(p)[bank] ?: continue
            for ((num, items) in pages.entries) {
                val total = items.sumOf { it.count }
                context.drawText(textRenderer, Text.literal("$p - Page $num: $total items"), x, yy, 0xDDDDDD, false)
                yy += 12
            }
        }
    }

    private fun renderPage(context: DrawContext, x: Int, y: Int, w: Int, h: Int, mouseX: Int, mouseY: Int) {
        val bank = selectedBankType ?: return
        val page = selectedPage ?: return
        val player = selectedPlayer ?: return
        val items = BankStorageManager.getPages(player, bank)[page] ?: return

        context.drawText(textRenderer, Text.literal("$bank - Page $page"), x, y - 12, 0xFFFFFF, false)

        val filtered = if (searchQuery.isNotBlank()) {
            items.filter { it.id.lowercase().contains(searchQuery) }
        } else items

        val cols = 8
        val slot = 20
        val rows = ceil(filtered.size / cols.toDouble()).toInt()
        maxScroll = max(0.0, rows * slot - h + 20.0)

        var idx = 0
        for (rec in filtered) {
            val stack = BankStorageManager.itemRecordToItemStack(rec)
            val row = idx / cols
            val col = idx % cols
            val sx = x + col * slot
            val sy = (y + row * slot - scrollOffset).toInt()
            if (sy in (y - slot)..(y + h)) {
                context.drawItem(stack, sx, sy)
                context.drawItemInSlot(textRenderer, stack, sx, sy)
                if (mouseX in sx..(sx + 16) && mouseY in sy..(sy + 16)) hoveredStack = stack
            }
            idx++
        }
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        scrollOffset = (scrollOffset - verticalAmount * 10.0).coerceIn(0.0, maxScroll)
        return true
    }

    override fun shouldPause(): Boolean = false
}
