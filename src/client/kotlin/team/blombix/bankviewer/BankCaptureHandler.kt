package team.blombix.bankviewer

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.slot.Slot

object BankCaptureHandler {

    private var lastSeenTitle: String? = null

    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client ->
            try {
                val screen = client.currentScreen ?: return@EndTick
                val title = screen.title?.string ?: return@EndTick

                // tytuł wygląda np. "Legacy Bank Page 1/5" lub "Main Bank Page 2/4"
                val match = Regex("""(Legacy|Main) Bank Page (\d+)/(\d+)""").find(title)
                if (match == null || screen !is HandledScreen<*>) return@EndTick

                if (title == lastSeenTitle) return@EndTick
                lastSeenTitle = title

                val bankType = match.groupValues[1]
                val pageNum = match.groupValues[2].toIntOrNull() ?: return@EndTick
                val playerName = client.session?.username ?: "unknown"

                val handler = screen.screenHandler
                val allSlots: List<Slot> = handler.slots
                val totalSlots = allSlots.size

                // many containers have player inventory at end; this heuristic keeps only menu slots
                val playerSlotCount = 36
                val firstPlayerSlotIndex = (totalSlots - playerSlotCount).coerceAtLeast(0)

                val records = mutableListOf<ItemRecord>()

                for (i in 0 until firstPlayerSlotIndex) {
                    val slot = allSlots[i]
                    val stack = slot.stack
                    if (!stack.isEmpty) {
                        val id = RegistryUtil.getItemId(stack)

                        // try read custom data component (if present) to keep full raw NBT, otherwise fallback to stack.nbt
                        var nbtString = ""
                        try {
                            val comp = stack.get(DataComponentTypes.CUSTOM_DATA)
                            if (comp is NbtComponent) {
                                // NbtComponent has 'nbt' field (NbtCompound) in mappings; use reflection safe-access
                                val nbtField = comp::class.java.getMethod("nbt")
                                val nbtObj = nbtField.invoke(comp) as? NbtCompound
                                if (nbtObj != null) nbtString = nbtObj.toString()
                            }
                        } catch (_: Throwable) {
                            // fallback to direct stack NBT if available
                            try {
                                val tagMethod = stack::class.java.getMethod("getNbt")
                                val tag = tagMethod.invoke(stack) as? NbtCompound
                                if (tag != null) nbtString = tag.toString()
                            } catch (_: Throwable) {
                                // ignore
                            }
                        }

                        records.add(ItemRecord(id, stack.count, nbtString))
                    }
                }

                if (records.isNotEmpty()) {
                    BankStorageManager.storePageForPlayer(playerName, bankType, pageNum, records)
                    println("Saved bank page: $playerName / $bankType / page $pageNum (${records.size} items)")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }
}
