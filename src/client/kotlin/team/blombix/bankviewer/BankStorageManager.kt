package team.blombix.bankviewer

import com.google.gson.GsonBuilder
import net.minecraft.client.MinecraftClient
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.StringNbtReader
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.io.File

data class ItemRecord(
    val id: String,
    val count: Int,
    val nbt: String = "{}"
)

object BankStorageManager {

    private val dataFolder = File(MinecraftClient.getInstance().runDirectory, "bankviewer_data")
    private val storage = mutableMapOf<String, MutableMap<String, MutableMap<Int, MutableList<ItemRecord>>>>()

    init {
        if (!dataFolder.exists()) dataFolder.mkdirs()
    }

    fun getPlayers(): List<String> = storage.keys.sorted()

    fun getBanksForPlayer(player: String): Map<String, MutableMap<Int, MutableList<ItemRecord>>> {
        return storage[player] ?: emptyMap()
    }

    fun getPages(player: String, bankType: String): Map<Int, MutableList<ItemRecord>> {
        return storage[player]?.get(bankType) ?: emptyMap()
    }

    fun getAggregateFor(player: String?, bankType: String?): Map<String, ItemRecord> {
        val players = if (player == null) storage.keys else listOf(player)
        val map = mutableMapOf<String, ItemRecord>()

        for (p in players) {
            val banks = storage[p] ?: continue
            for ((type, pages) in banks) {
                if (bankType != null && bankType != type) continue
                for (page in pages.values) {
                    for (rec in page) {
                        val existing = map[rec.id]
                        if (existing == null) {
                            map[rec.id] = rec.copy()
                        } else {
                            map[rec.id] = existing.copy(count = existing.count + rec.count)
                        }
                    }
                }
            }
        }
        return map
    }

    fun storePageForPlayer(player: String, bankType: String, pageNum: Int, records: List<ItemRecord>) {
        val playerBanks = storage.getOrPut(player) { mutableMapOf() }
        val pages = playerBanks.getOrPut(bankType) { mutableMapOf() }
        pages[pageNum] = records.toMutableList()

        saveToDisk(player)
    }

    private fun getFileForPlayer(player: String): File {
        return File(dataFolder, "$player.json")
    }

    private fun saveToDisk(player: String) {
        val file = getFileForPlayer(player)
        val json = GsonBuilder().setPrettyPrinting().create().toJson(storage[player])
        file.writeText(json)
    }

    fun loadFromDisk() {
        if (!dataFolder.exists()) return
        dataFolder.listFiles { f -> f.extension == "json" }?.forEach { file ->
            val player = file.nameWithoutExtension
            try {
                val mapType = mutableMapOf<String, MutableMap<Int, MutableList<ItemRecord>>>()::class.java
                val map = GsonBuilder().create().fromJson(file.readText(), mapType)
                storage[player] = map
            } catch (_: Exception) {
            }
        }
    }

    fun itemRecordToItemStack(record: ItemRecord): ItemStack {
        val id = try {
            Identifier.of(record.id)
        } catch (e: Exception) {
            Identifier.of("minecraft:air")
        }

        val item = Registries.ITEM.get(id)
        val stack = ItemStack(item, record.count)

        if (record.nbt.isNotEmpty()) {
            try {
                val nbt = StringNbtReader.parse(record.nbt)
                val nbtComp = NbtComponent.of(nbt)
                stack.set(DataComponentTypes.CUSTOM_DATA, nbtComp)

                // DISPLAY
                if (nbt.contains("display", NbtElement.COMPOUND_TYPE.toInt())) {
                    val display = nbt.getCompound("display")

                    // NAME
                    if (display.contains("Name", NbtElement.STRING_TYPE.toInt())) {
                        val nameJson = display.getString("Name")
                        val parsed = try {
                            val json = com.google.gson.JsonParser.parseString(nameJson)
                            net.minecraft.text.TextCodecs.CODEC
                                .parse(com.mojang.serialization.JsonOps.INSTANCE, json)
                                .result().orElse(Text.literal(nameJson))
                        } catch (_: Exception) {
                            Text.literal(nameJson.replace("§", "§"))
                        }
                        stack.set(DataComponentTypes.CUSTOM_NAME, parsed)
                    }

                    // LORE
                    if (display.contains("Lore", NbtElement.LIST_TYPE.toInt())) {
                        val loreList = display.getList("Lore", NbtElement.STRING_TYPE.toInt())
                        val loreTexts = mutableListOf<Text>()
                        for (i in 0 until loreList.size) {
                            val loreRaw = loreList.getString(i)
                            val parsed = try {
                                val json = com.google.gson.JsonParser.parseString(loreRaw)
                                net.minecraft.text.TextCodecs.CODEC
                                    .parse(com.mojang.serialization.JsonOps.INSTANCE, json)
                                    .result().orElse(Text.literal(loreRaw))
                            } catch (_: Exception) {
                                Text.literal(loreRaw.replace("§", "§"))
                            }
                            loreTexts.add(parsed)
                        }
                        if (loreTexts.isNotEmpty()) {
                            stack.set(DataComponentTypes.LORE, net.minecraft.component.type.LoreComponent(loreTexts))
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return stack
    }
}
