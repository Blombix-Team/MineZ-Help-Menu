package team.blombix.bankviewer

import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

object RegistryUtil {
    fun getItemId(stack: ItemStack): String {
        return try {
            val id = Registries.ITEM.getId(stack.item)
            id.toString()
        } catch (e: Exception) {
            stack.item.translationKey ?: "minecraft:barrier"
        }
    }

    fun identifierOf(id: String): Identifier {
        return try {
            if (id.contains(":")) {
                val (namespace, path) = id.split(":", limit = 2)
                Identifier.of(namespace, path)
            } else Identifier.ofVanilla(id)
        } catch (e: Exception) {
            Identifier.ofVanilla("barrier")
        }
    }
}
