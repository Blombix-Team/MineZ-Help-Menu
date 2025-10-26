package team.blombix.bankviewer

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.screen.slot.Slot
import team.blombix.DebugConfig

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

                // tune this depending on GUI layout (usually 36/45)
                val playerSlotCount = 45
                val firstPlayerSlotIndex = (totalSlots - playerSlotCount).coerceAtLeast(0)

                val records = mutableListOf<ItemRecord>()

                for (i in 0 until firstPlayerSlotIndex) {
                    val slot = allSlots[i]
                    val stack = slot.stack
                    if (stack.isEmpty) continue

                    val id = RegistryUtil.getItemId(stack)
                    var nbtString = "{}"

                    try {
                        // użyj aktualnego registry managera klienta, nie EMPTY
                        val registryManager = client.world?.registryManager ?: DynamicRegistryManager.EMPTY

                        // encode() w 1.21.1 serializuje wszystko, łącznie z DataComponents
                        val encoded = stack.encode(registryManager)

                        if (encoded is NbtCompound) {
                            nbtString = encoded.toString()
                        } else {
                            // jeśli CODEC też coś da, to jeszcze lepiej
                            val alt = ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack).result().orElse(null)
                            if (alt != null) {
                                nbtString = alt.toString()
                            }
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        nbtString = "{}"
                    }

                    records.add(ItemRecord(id, stack.count, nbtString))
                }

                if (records.isNotEmpty()) {
                    BankStorageManager.storePageForPlayer(playerName, bankType, pageNum, records)
                    DebugConfig.log(
                        "Saved %s page %d for %s (%d items)",
                        bankType,
                        pageNum,
                        playerName,
                        records.size
                    )
                }

            } catch (t: Throwable) {
                t.printStackTrace()
            }
        })
    }
}
