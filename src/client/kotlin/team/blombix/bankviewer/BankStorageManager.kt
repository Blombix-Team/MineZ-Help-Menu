package team.blombix.bankviewer

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.StringNbtReader
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.io.File
import java.lang.Exception
import java.lang.reflect.Type
import kotlin.collections.set

data class ItemRecord(val id: String, var count: Int, val nbt: String = "")

object BankStorageManager {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val dataDir = File("bankviewer").apply { if (!exists()) mkdirs() }
    private val cache = mutableMapOf<String, MutableMap<String, MutableMap<Int, MutableList<ItemRecord>>>>()

    private fun getPlayerFile(player: String): File = File(dataDir, "$player.json")

    fun storePageForPlayer(player: String, bankType: String, page: Int, items: List<ItemRecord>) {
        val playerMap = cache.computeIfAbsent(player) { mutableMapOf() }
        val bankMap = playerMap.computeIfAbsent(bankType) { mutableMapOf() }
        bankMap[page] = items.toMutableList()
        // ensure dir
        if (!dataDir.exists()) dataDir.mkdirs()
        saveToFile(player)
    }

    private fun saveToFile(player: String) {
        try {
            val file = getPlayerFile(player)
            val toSave = cache[player] ?: emptyMap<String, MutableMap<Int, MutableList<ItemRecord>>>()
            file.writeText(gson.toJson(toSave))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPlayers(): List<String> {
        // list files in folder
        return dataDir.listFiles()?.mapNotNull { f ->
            if (f.isFile && f.extension == "json") f.nameWithoutExtension else null
        }?.sorted() ?: emptyList()
    }

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

    /**
     * Aggregate by id|nbt preserving distinct variants. Returns map key->ItemRecord (first seen variant),
     * counts summed for equal id+nbt.
     */
    fun getAggregateFor(player: String?, bankType: String?): Map<String, ItemRecord> {
        val result = mutableMapOf<String, ItemRecord>()
        val players = if (player == null) getPlayers() else listOf(player)
        for (p in players) {
            val banks = getBanksForPlayer(p)
            for ((bt, pages) in banks) {
                if (bankType != null && bankType != bt) continue
                for (items in pages.values) {
                    for (rec in items) {
                        val key = "${rec.id}|${rec.nbt}"
                        val existing = result[key]
                        if (existing == null) result[key] = rec.copy()
                        else result[key] = existing.copy(count = existing.count + rec.count)
                    }
                }
            }
        }
        return result
    }

    /**
     * Build ItemStack from ItemRecord. Uses ItemStack.fromNbt(DynamicRegistryManager.EMPTY, nbt) preferred.
     */
    fun itemRecordToItemStack(rec: ItemRecord): ItemStack {
        val id = try { Identifier.of(rec.id) } catch (_: Exception) { Identifier.of("minecraft:air") }
        val item = Registries.ITEM.get(id)
        val fallback = ItemStack(item, rec.count)

        if (rec.nbt.isBlank()) return fallback

        try {
            val nbt: NbtCompound = StringNbtReader.parse(rec.nbt)

            // ensure Count tag present so fromNbt sets correct count
            if (!nbt.contains("Count")) nbt.putByte("Count", rec.count.toByte())

            val opt = ItemStack.fromNbt(DynamicRegistryManager.EMPTY, nbt)
            if (opt.isPresent) {
                val full = opt.get()
                if (full.count <= 0) full.count = rec.count
                return full
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }

        // fallback (should rarely be used) — return a simple stack with custom name/lore components if possible
        try {
            val nbt2 = StringNbtReader.parse(rec.nbt)
            // set custom name/lore via DataComponents so at least tooltip has display
            // but primary path is ItemStack.fromNbt above
            // (we keep fallback minimal)
        } catch (_: Throwable) {}
        return fallback
    }
}
