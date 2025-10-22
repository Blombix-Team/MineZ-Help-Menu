package team.blombix.bankviewer

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.slot.Slot

object BankCaptureHandler {

    private var lastSeenTitle: String? = null

    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient ->
            val screen = client.currentScreen
            val title = screen?.title?.string ?: return@EndTick

            val match = Regex("""(Legacy|Main) Bank Page (\d+)/(\d+)""").find(title)
            if (match == null || screen !is HandledScreen<*>) return@EndTick
            if (title == lastSeenTitle) return@EndTick
            lastSeenTitle = title

            try {
                val bankType = match.groupValues[1]
                val pageNum = match.groupValues[2].toIntOrNull() ?: return@EndTick
                val playerName = client.session?.username ?: "unknown"

                val handler = screen.screenHandler
                val allSlots: List<Slot> = handler.slots
                val totalSlots = allSlots.size

                // assume last 36 slots are player inventory (hotbar + player)
                val playerSlotCount = 36
                val firstPlayerSlotIndex = (totalSlots - playerSlotCount).coerceAtLeast(0)

                val records = mutableListOf<ItemRecord>()

                for (i in 0 until firstPlayerSlotIndex) {
                    val slot = allSlots[i]
                    val stack = slot.stack
                    if (stack.isEmpty) continue

                    val id = RegistryUtil.getItemId(stack)

                    // write full NBT (this includes display, Enchantments, SkullOwner, Potion, CustomModelData, etc.)
                    val nbtString = try {
                        // zapis pełnego NBT stacka z użyciem encode() (działa w 1.21.1+)
                        val compound = stack.encode(net.minecraft.registry.DynamicRegistryManager.EMPTY)
                        compound.toString()
                    } catch (_: Throwable) {
                        "{}"
                    }


                    records.add(ItemRecord(id, stack.count, nbtString))
                }

                if (records.isNotEmpty()) {
                    BankStorageManager.storePageForPlayer(playerName, bankType, pageNum, records)
                    println("BankCaptureHandler: saved $bankType page $pageNum for $playerName (${records.size} items)")
                }

            } catch (t: Throwable) {
                t.printStackTrace()
            }
        })
    }
}
