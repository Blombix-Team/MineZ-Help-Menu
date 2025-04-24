package team.blombix.tooltips

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
                Items.CARROT -> lines.add(
                    Text.literal("Thirst: ")
                        .formatted(Formatting.AQUA)
                        .append(
                            Text.literal("+3").formatted(Formatting.DARK_AQUA)
                        )
                )

                Items.APPLE -> lines.add(
                    Text.literal("Thirst: ")
                        .formatted(Formatting.AQUA)
                        .append(
                            Text.literal("+2").formatted(Formatting.DARK_AQUA)
                        )
                )

                Items.GOLDEN_APPLE -> lines.add(
                    Text.literal("Thirst: ")
                        .formatted(Formatting.AQUA)
                        .append(
                            Text.literal("+2").formatted(Formatting.DARK_AQUA)
                        )
                )

                Items.BEETROOT -> lines.add(
                    Text.literal("Thirst: ")
                        .formatted(Formatting.AQUA)
                        .append(
                            Text.literal("+2").formatted(Formatting.DARK_AQUA)
                        )
                )

                Items.MELON_SLICE -> lines.add(
                    Text.literal("Thirst: ")
                        .formatted(Formatting.AQUA)
                        .append(
                            Text.literal("+3").formatted(Formatting.DARK_AQUA)
                        )
                )

                Items.BEETROOT_SOUP -> lines.add(
                    Text.literal("Thirst: ")
                        .formatted(Formatting.AQUA)
                        .append(
                            Text.literal("+10").formatted(Formatting.DARK_AQUA)
                        )
                )

                Items.MUSHROOM_STEW -> lines.add(
                    Text.literal("Thirst: ")
                        .formatted(Formatting.AQUA)
                        .append(
                            Text.literal("+1").formatted(Formatting.DARK_AQUA)
                        )
                )

                Items.RABBIT_STEW -> lines.add(
                    Text.literal("Thirst: ")
                        .formatted(Formatting.AQUA)
                        .append(
                            Text.literal("+1").formatted(Formatting.DARK_AQUA)
                        )
                )
            }
        })
    }
}
