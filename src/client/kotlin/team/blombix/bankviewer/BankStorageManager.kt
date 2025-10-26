package team.blombix.bankviewer

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.StringNbtReader
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.io.File
import java.lang.reflect.Type

data class ItemRecord(val id: String, val count: Int, val nbt: String = "")

object BankStorageManager {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val rootDir = File("bankviewer")
    private val cache = mutableMapOf<String, MutableMap<String, MutableMap<Int, MutableList<ItemRecord>>>>()

    init {
        if (!rootDir.exists()) rootDir.mkdirs()
    }

    private fun getPlayerFile(player: String) = File(rootDir, "$player.json")

    fun storePageForPlayer(player: String, bankType: String, page: Int, items: List<ItemRecord>) {
        val playerMap = cache.computeIfAbsent(player) { mutableMapOf() }
        val bankMap = playerMap.computeIfAbsent(bankType) { mutableMapOf() }
        bankMap[page] = items.toMutableList()
        saveToFile(player)
    }

    private fun saveToFile(player: String) {
        try {
            val file = getPlayerFile(player)
            val json = gson.toJson(cache[player])
            file.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPlayers(): List<String> = rootDir.listFiles()?.mapNotNull {
        if (it.extension == "json") it.nameWithoutExtension else null
    } ?: emptyList()

    fun getBanksForPlayer(player: String): Map<String, Map<Int, List<ItemRecord>>> {
        if (!cache.containsKey(player)) {
            val file = getPlayerFile(player)
            if (file.exists()) {
                try {
                    val type: Type = object : TypeToken<MutableMap<String, MutableMap<Int, MutableList<ItemRecord>>>>() {}.type
                    val data: MutableMap<String, MutableMap<Int, MutableList<ItemRecord>>>? = gson.fromJson(file.readText(), type)
                    cache[player] = data ?: mutableMapOf()
                } catch (e: Exception) {
                    e.printStackTrace()
                    cache[player] = mutableMapOf()
                }
            } else {
                cache[player] = mutableMapOf()
            }
        }
        return cache[player] ?: emptyMap()
    }

    fun getPages(player: String, bankType: String): Map<Int, List<ItemRecord>> {
        return getBanksForPlayer(player)[bankType] ?: emptyMap()
    }

    fun getPagesForDisplay(player: String?, bankType: String): List<Int> {
        return if (player == null) {
            cache.values.flatMap { it[bankType]?.keys ?: emptySet() }.distinct().sorted()
        } else {
            getBanksForPlayer(player)[bankType]?.keys?.sorted() ?: emptyList()
        }
    }

    fun getAggregateFor(player: String?, bankType: String?): Map<String, ItemRecord> {
        val result = mutableMapOf<String, ItemRecord>()
        val players = if (player == null) getPlayers() else listOf(player)
        for (p in players) {
            val banks = getBanksForPlayer(p)
            for ((bt, pages) in banks) {
                if (bankType != null && bankType != bt) continue
                for (items in pages.values) {
                    for (rec in items) {
                        // Key by id + nbt to not merge different named items
                        val key = "${rec.id}|${rec.nbt}"
                        val existing = result[key]
                        if (existing == null) result[key] = ItemRecord(rec.id, rec.count, rec.nbt)
                        else result[key] = ItemRecord(existing.id, existing.count + rec.count, existing.nbt)
                    }
                }
            }
        }
        return result
    }

    fun itemRecordToItemStack(rec: ItemRecord): ItemStack {
        val item = try {
            Registries.ITEM.get(Identifier.of(rec.id))
        } catch (_: Exception) {
            Registries.ITEM.get(Identifier.of("minecraft:air"))
        }

        val stack = ItemStack(item, rec.count.coerceAtLeast(1))

        if (rec.nbt.isNotBlank() && rec.nbt != "{}") {
            try {
                // Kod używający CODEC do sparsowania NBT
                val nbt = StringNbtReader.parse(rec.nbt) // parse string do NbtCompound
                val parsed = ItemStack.CODEC.parse(NbtOps.INSTANCE, nbt)
                val finalStack = parsed.result().orElse(stack)
                finalStack.count = rec.count
                return finalStack
            } catch (_: Throwable) {
                // fallback – stack bez NBT
            }
        }

        return stack
    }



}
