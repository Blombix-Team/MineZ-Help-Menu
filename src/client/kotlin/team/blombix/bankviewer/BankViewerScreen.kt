package team.blombix.bankviewer

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import kotlin.math.ceil
import kotlin.math.max

class BankViewerScreen : Screen(Text.literal("Bank Viewer")) {

    private val mc = MinecraftClient.getInstance()
    private var selectedPlayer: String? = null
    private var selectedBankType: String? = null
    private var selectedPage: Int? = null

    private var scrollOffset = 0.0
    private var maxScroll = 0.0

    private var hoveredStack: ItemStack? = null

    override fun init() {
        super.init()
        clearChildren()

        var x = 10
        val y = 10

        addDrawableChild(ButtonWidget.builder(Text.literal("All")) {
            selectedPlayer = null
            init()
        }.dimensions(x, y, 60, 20).build())
        x += 70

        for (p in BankStorageManager.getPlayers()) {
            addDrawableChild(ButtonWidget.builder(Text.literal(p)) {
                selectedPlayer = p
                init()
            }.dimensions(x, y, 80, 20).build())
            x += 90
        }

        var by = 40
        addDrawableChild(ButtonWidget.builder(Text.literal("Aggregate")) {
            selectedBankType = null
            selectedPage = null
            init()
        }.dimensions(10, by, 100, 20).build())
        by += 24

        for (bankType in gatherBankTypes()) {
            addDrawableChild(ButtonWidget.builder(Text.literal(bankType)) {
                selectedBankType = bankType
                selectedPage = null
                init()
            }.dimensions(10, by, 100, 20).build())
            by += 24

            val playerForPages = selectedPlayer ?: continue
            val pages = BankStorageManager.getPages(playerForPages, bankType).keys.sorted()
            for (p in pages) {
                addDrawableChild(ButtonWidget.builder(Text.literal("Page $p")) {
                    selectedBankType = bankType
                    selectedPage = p
                    init()
                }.dimensions(20, by, 80, 18).build())
                by += 20
            }
            by += 8
        }

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
        val title = Text.literal("Bank Viewer")
        context.drawText(textRenderer, title, width / 2 - textRenderer.getWidth(title) / 2, 6, 0xFFFFFF, false)
        super.render(context, mouseX, mouseY, delta)

        val x = 130
        val y = 40
        val w = width - x - 20
        val h = height - y - 50
        context.fill(x - 6, y - 6, x + w + 6, y + h + 6, 0x55000000)

        hoveredStack = null

        when {
            selectedBankType == null -> renderAggregate(context, x, y, w, h, mouseX, mouseY)
            selectedPage == null -> renderOverview(context, x, y, w, h)
            else -> renderPage(context, x, y, w, h, mouseX, mouseY)
        }

        hoveredStack?.let { stack ->
            try {
                val tooltip = stack.getTooltip(
                    net.minecraft.item.Item.TooltipContext.DEFAULT,
                    mc.player,
                    TooltipType.BASIC
                )
                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY)
            } catch (_: Throwable) {
                context.drawTooltip(textRenderer, listOf(Text.literal(stack.name.string)), mouseX, mouseY)
            }
        }

    }

    private fun renderBackground(context: DrawContext) {}

    private fun renderAggregate(context: DrawContext, x: Int, y: Int, w: Int, h: Int, mouseX: Int, mouseY: Int) {
        val agg = BankStorageManager.getAggregateFor(selectedPlayer, null)
        val cols = 8
        val slot = 20
        val rows = ceil(agg.size / cols.toDouble()).toInt()
        maxScroll = max(0.0, rows * slot - h + 20.0)

        val startY = y + 14
        var idx = 0
        for ((_, rec) in agg.entries) {
            val stack = BankStorageManager.itemRecordToItemStack(rec)
            val row = idx / cols
            val col = idx % cols
            val sx = x + col * slot
            val sy = (startY + row * slot - scrollOffset).toInt()

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
        context.drawText(textRenderer, Text.literal("$bank - Overview"), x, y, 0xFFFFFF, false)
        var yy = y + 14
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

        val cols = 8
        val slot = 20
        val rows = ceil(items.size / cols.toDouble()).toInt()
        maxScroll = max(0.0, rows * slot - h + 20.0)

        var idx = 0
        val startY = y + 14
        for (rec in items) {
            val stack = BankStorageManager.itemRecordToItemStack(rec)
            val row = idx / cols
            val col = idx % cols
            val sx = x + col * slot
            val sy = (startY + row * slot - scrollOffset).toInt()

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
