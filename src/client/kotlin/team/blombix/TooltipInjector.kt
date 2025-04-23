package team.blombix

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object TooltipInjector {
    fun register() {
        ItemTooltipCallback.EVENT.register(ItemTooltipCallback { stack: ItemStack, _, _: TooltipType, lines: MutableList<Text> ->
            when (stack.item) {
                Items.CARROT -> lines.add(Text.literal("Thirst: +3").formatted(Formatting.DARK_BLUE))
                Items.APPLE -> lines.add(Text.literal("Thirst: +1").formatted(Formatting.DARK_BLUE))
                Items.BEETROOT -> lines.add(Text.literal("Thirst: +2").formatted(Formatting.DARK_BLUE))
                Items.MELON_SLICE -> lines.add(Text.literal("Thirst: +3").formatted(Formatting.DARK_BLUE))
            }
        })
    }
}
