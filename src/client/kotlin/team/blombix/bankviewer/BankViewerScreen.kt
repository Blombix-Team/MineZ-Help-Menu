package team.blombix.bankviewer

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.toast.SystemToast
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class BankViewerScreen : Screen(Text.literal("Bank Viewer")) {

    private val mc = MinecraftClient.getInstance()

    private var selectedPlayer: String? = null
    private var selectedBankType: String? = null
    private var selectedPage: Int? = null
    private var searchQuery = ""

    private var scrollOffset = 0.0
    private var maxScroll = 0.0
    private var accountScroll = 0.0
    private var maxAccountScroll = 0.0

    private var hoveredStack: ItemStack? = null
    private var hoveredOwner: String? = null
    private var hoveredPageLabel: String? = null

    private lateinit var searchBox: TextFieldWidget
    private var cachedAggregate: List<Triple<ItemRecord, String, Pair<String, Int>>>? = null

    private val mainPages = listOf(1, 2, 3, 4)
    private val legacyPages = listOf(1, 2, 3)

    override fun init() {
        super.init()
        clearChildren()
        cachedAggregate = null

        val leftPanelWidth = 160
        val rightPanelWidth = (width * 0.25).toInt()
        val topY = 40
        val bottomHeight = 40

        // --- SEARCH BOX ---
        val searchWidth = width / 5
        val searchX = 10
        searchBox = TextFieldWidget(textRenderer, searchX, 10, searchWidth, 20, Text.literal("Search"))
        searchBox.setChangedListener { searchQuery = it.lowercase() }
        addDrawableChild(searchBox)

        // --- LEFT PANEL (BANKS) ---
        val leftX = 10
        var leftY = topY + 10
        val buttonW = leftPanelWidth - 40
        val buttonH = 20
        val spacing = 8
        val centerButtonX = leftX + (leftPanelWidth - buttonW) / 2

        val leftButtons = listOf(
            "All" to { openAll() },
            "Main 1" to { openBank("Main", 1) },
            "Main 2" to { openBank("Main", 2) },
            "Main 3" to { openBank("Main", 3) },
            "Main 4" to { openBank("Main", 4) },
            "Legacy 1" to { openBank("Legacy", 1) },
            "Legacy 2" to { openBank("Legacy", 2) },
            "Legacy 3" to { openBank("Legacy", 3) }
        )

        for ((label, action) in leftButtons) {
            addDrawableChild(
                ButtonWidget.builder(Text.literal(label)) { action() }
                    .dimensions(centerButtonX, leftY, buttonW, buttonH).build()
            )
            leftY += buttonH + spacing
        }

        // --- SETTINGS BUTTON ---
        addDrawableChild(
            ButtonWidget.builder(Text.literal("Settings")) {
                mc.toastManager.add(
                    SystemToast(
                        SystemToast.Type.NARRATOR_TOGGLE,
                        Text.literal("BankViewer"),
                        Text.literal("Work in progress")
                    )
                )
                //TODO: BankViewer Settings
            }.dimensions(centerButtonX, height - bottomHeight, buttonW, buttonH).build()
        )

        // --- RIGHT PANEL (PLAYERS) ---
        val rightX = width - rightPanelWidth + 10
        val rightY = topY + 10
        val players = BankStorageManager.getPlayers().sorted()
        val visibleAccountCount = ((height - bottomHeight - rightY) / (buttonH + spacing)).coerceAtLeast(1)
        maxAccountScroll = max(0.0, (players.size - visibleAccountCount).toDouble())

        val startIdx = accountScroll.toInt().coerceAtLeast(0)
        val endIdx = min(players.size, startIdx + visibleAccountCount)
        var yCursor = rightY

        for (i in startIdx until endIdx) {
            val p = players[i]
            addDrawableChild(
                ButtonWidget.builder(Text.literal(p)) {
                    selectedPlayer = p
                    selectedBankType = null
                    selectedPage = null
                    init()
                }.dimensions(rightX, yCursor, rightPanelWidth - 30, buttonH).build()
            )
            yCursor += buttonH + spacing
        }

        // --- CLOSE BUTTON ---
        addDrawableChild(
            ButtonWidget.builder(Text.literal("Close")) {
                mc.setScreen(null as Screen?)
            }.dimensions(width - rightPanelWidth + 10, height - bottomHeight, rightPanelWidth - 30, buttonH).build()
        )
    }

    private fun openAll() {
        selectedPlayer = null
        selectedBankType = null
        selectedPage = null
        init()
    }

    private fun openBank(type: String, page: Int?) {
        selectedBankType = type
        selectedPage = page
        init()
    }

    private fun renderAggregate(
        context: DrawContext, x: Int, y: Int, w: Int, h: Int, mouseX: Int, mouseY: Int
    ) {
        if (cachedAggregate == null) cachedAggregate = buildDetailedAggregate()

        val filtered = cachedAggregate!!.filter {
            searchQuery.isBlank() || it.first.id.contains(searchQuery, true)
        }

        val slot = 20
        val cols = max(1, w / slot)
        val rows = ceil(filtered.size / cols.toDouble()).toInt()
        maxScroll = max(0.0, rows * slot - h + 20.0)
        val rowOffset = (scrollOffset / slot).toInt()
        val startIndex = rowOffset * cols
        val visibleRows = (h / slot).coerceAtLeast(1)
        val endIndex = min(filtered.size, (rowOffset + visibleRows + 1) * cols)

        for (i in startIndex until endIndex) {
            val (rec, owner, bankPage) = filtered[i]
            val stack = BankStorageManager.itemRecordToItemStack(rec)

            val localIndex = i - startIndex
            val row = localIndex / cols
            val col = localIndex % cols
            val sx = x + col * slot
            val sy = (y + row * slot) - (scrollOffset % slot).toInt()

            if (sy in (y - slot)..(y + h)) {
                context.drawItem(stack, sx, sy)
                context.drawItemInSlot(textRenderer, stack, sx, sy)
                if (mouseX in sx..(sx + 16) && mouseY in sy..(sy + 16)) {
                    hoveredStack = stack
                    hoveredOwner = owner
                    hoveredPageLabel = "${bankPage.first} ${bankPage.second}"
                }
            }
        }

        // scrollbar
        if (maxScroll > 0) {
            val scrollHeight = (h * (h / (h + maxScroll))).toInt()
            val scrollY = (y + (scrollOffset / maxScroll * (h - scrollHeight))).toInt()
            context.fill(x + w - 4, scrollY, x + w, scrollY + scrollHeight, 0xAAFFFFFF.toInt())
        }
    }

    private fun renderPage(
        context: DrawContext, x: Int, y: Int, w: Int, h: Int, mouseX: Int, mouseY: Int
    ) {
        val bank = selectedBankType ?: return
        val page = selectedPage ?: return
        val player = selectedPlayer ?: return
        val pages = BankStorageManager.getPages(player, bank)
        val items = pages[page] ?: return

        val filtered = if (searchQuery.isBlank()) items else items.filter {
            it.id.contains(searchQuery, true)
        }

        val slot = 18
        val cols = max(1, w / slot)
        val rows = ceil(filtered.size / cols.toDouble()).toInt()
        maxScroll = max(0.0, rows * slot - h + 20.0)
        val rowOffset = (scrollOffset / slot).toInt()
        val startIndex = rowOffset * cols
        val visibleRows = (h / slot).coerceAtLeast(1)
        val endIndex = min(filtered.size, (rowOffset + visibleRows + 1) * cols)

        for (i in startIndex until endIndex) {
            val rec = filtered[i]
            val stack = BankStorageManager.itemRecordToItemStack(rec)

            val localIndex = i - startIndex
            val row = localIndex / cols
            val col = localIndex % cols
            val sx = x + col * slot
            val sy = (y + row * slot) - (scrollOffset % slot).toInt()

            if (sy in (y - slot)..(y + h)) {
                context.drawItem(stack, sx, sy)
                context.drawItemInSlot(textRenderer, stack, sx, sy)
                if (mouseX in sx..(sx + 16) && mouseY in sy..(sy + 16)) {
                    hoveredStack = stack
                    hoveredOwner = player
                    hoveredPageLabel = "$bank $page"
                }
            }
        }

        // scrollbar
        if (maxScroll > 0) {
            val scrollHeight = (h * (h / (h + maxScroll))).toInt()
            val scrollY = (y + (scrollOffset / maxScroll * (h - scrollHeight))).toInt()
            context.fill(x + w - 4, scrollY, x + w, scrollY + scrollHeight, 0xAAFFFFFF.toInt())
        }
    }

    private fun buildDetailedAggregate(): List<Triple<ItemRecord, String, Pair<String, Int>>> {
        val list = mutableListOf<Triple<ItemRecord, String, Pair<String, Int>>>()
        val players = BankStorageManager.getPlayers()
        for (p in players) {
            val banks = BankStorageManager.getBanksForPlayer(p)
            for ((bankType, pages) in banks) {
                for ((pageNum, items) in pages) {
                    for (rec in items) {
                        list.add(Triple(rec, p, Pair(bankType, pageNum)))
                    }
                }
            }
        }
        return list
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context, mouseX, mouseY, delta)

        val leftPanelWidth = 160
        val rightPanelWidth = (width * 0.25).toInt()
        val itemPanelX = leftPanelWidth + 25
        val itemPanelW = width - leftPanelWidth - rightPanelWidth - 60
        val itemPanelY = 50
        val itemPanelH = height - 140

        // backgrounds
        context.fill(0, 0, width, height, 0x66000000)

        // search
        searchBox.render(context, mouseX, mouseY, delta)
        if (searchBox.text.isEmpty()) {
            context.drawText(
                textRenderer,
                Text.literal("Search items..."),
                searchBox.x + 4,
                searchBox.y + 6,
                0x777777,
                false
            )
        }

        hoveredStack = null
        hoveredOwner = null
        hoveredPageLabel = null

        if (selectedBankType == null) {
            renderAggregate(context, itemPanelX, itemPanelY, itemPanelW, itemPanelH, mouseX, mouseY)
        } else if (selectedPage == null) {
            context.drawText(
                textRenderer,
                Text.literal("Select a bank to view pages"),
                itemPanelX,
                itemPanelY,
                0xFFFFFF,
                false
            )
        } else {
            renderPage(context, itemPanelX, itemPanelY, itemPanelW, itemPanelH, mouseX, mouseY)
        }

        hoveredStack?.let { stack ->
            val tooltipList = stack.getTooltip(
                Item.TooltipContext.DEFAULT,
                mc.player,
                TooltipType.ADVANCED
            )
            val lines = tooltipList.toMutableList()
            hoveredOwner?.let { owner ->
                hoveredPageLabel?.let { pageLabel ->
                    lines.add(Text.literal("§7======= §8[§3BankViewer§8]§7 ======="))
                    lines.add(Text.literal("§3Account: §7$owner"))
                    lines.add(Text.literal("§3Page: §7$pageLabel"))
                }
            }
            context.drawTooltip(textRenderer, lines, mouseX, mouseY)
        }





        super.render(context, mouseX, mouseY, delta)
    }

    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.fill(0, 0, width, height, 0x88000000.toInt())
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        val rightPanelWidth = (width * 0.25).toInt()
        if (mouseX >= width - rightPanelWidth - 5) {
            accountScroll = (accountScroll - verticalAmount).coerceIn(0.0, maxAccountScroll)
            init()
            return true
        }
        if (maxScroll > 0) {
            scrollOffset = (scrollOffset - verticalAmount * 20.0).coerceIn(0.0, maxScroll)
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    // ESC closes screen
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == 256) { // GLFW_KEY_ESCAPE
            mc.setScreen(null as Screen?)
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun shouldPause(): Boolean = false
}
