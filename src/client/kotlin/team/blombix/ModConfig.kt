package team.blombix

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.io.File

object ModConfig {
    var showWaterDropIcon: Boolean = true
    var enableExpGradient: Boolean = true

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val configFile: File = FabricLoader.getInstance().configDir.resolve("minezmod_config.json").toFile()

    fun load() {
        if (!configFile.exists()) {
            save()
            return
        }

        try {
            val loaded = gson.fromJson(configFile.readText(), ModConfig::class.java)
            this.showWaterDropIcon = loaded.showWaterDropIcon
            this.enableExpGradient = loaded.enableExpGradient
        } catch (e: Exception) {
            println("Nie udało się załadować configu: ${e.message}")
        }
    }

    fun save() {
        try {
            configFile.writeText(gson.toJson(this))
        } catch (e: Exception) {
            println("Nie udało się zapisać configu: ${e.message}")
        }
    }
}
