package team.blombix.mixin.client;

//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.gui.widget.TexturedButtonWidget;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//import team.blombix.screens.HelpMenuScreenGettingStarted;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends HandledScreen<ScreenHandler> {
    @Unique
    protected int x;
    @Unique
    protected int y;

    public InventoryScreenMixin(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }


    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        //this.addDrawableChild(new TexturedButtonWidget(x + 152, y + 6, 20, 18, 0, 0, 18, new Identifier("modid", "textures/gui/eq_button.png"), 256, 256, button -> MinecraftClient.getInstance().setScreen(new HelpMenuScreenGettingStarted()), Text.literal("Otwórz custom GUI")));
        //TODO:Zrobić dzałający przycisk w eq
    }
}
