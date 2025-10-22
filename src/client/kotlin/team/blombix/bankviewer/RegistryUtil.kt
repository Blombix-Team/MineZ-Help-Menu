package team.blombix.bankviewer

import net.minecraft.registry.Registries
import net.minecraft.item.ItemStack

object RegistryUtil {
    fun getItemId(stack: ItemStack): String {
        return try {
            Registries.ITEM.getId(stack.item).toString()
        } catch (_: Exception) {
            "minecraft:air"
        }
    }
}
