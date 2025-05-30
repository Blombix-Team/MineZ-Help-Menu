package team.blombix.navigation


import com.google.gson.Gson
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.lang.reflect.Type

object LocationManager {
    private val gson = Gson()
    private val file: File = File(FabricLoader.getInstance().configDir.toFile(), "location.json")
    private val type: Type = object : com.google.gson.reflect.TypeToken<MutableList<Location>>() {}.type

    private val locations: MutableList<Location> = mutableListOf()

    fun loadLocations() {
        if (!file.exists()) {
            saveLocations()
            return
        }

        try {
            val content = file.readText()
            val loaded = gson.fromJson<MutableList<Location>>(content, type)
            locations.clear()
            locations.addAll(loaded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveLocations() {
        try {
            file.parentFile.mkdirs()
            file.writeText(gson.toJson(locations))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addLocation(location: Location) {
        locations.add(location)
        saveLocations()
    }

    fun getLocationByName(name: String): Location? {
        return locations.find { it.name.equals(name, ignoreCase = true) }
    }

    fun getAll(): List<Location> = locations
}