package team.blombix

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.ResourcePackActivationType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory


object MineZHelpMenu : ModInitializer {
    private val logger = LoggerFactory.getLogger("minez-help-menu")

    override fun onInitialize() {
        logger.info("[==============================================]")
        logger.info(" ")
        logger.info("MineZ Help Menu Mod")
        logger.info("Authors:")
        logger.info("Bubix, ChefMadCat")
        logger.info(" ")
        logger.info("Contributors:")
        logger.info("BasicAly")
        logger.info("All MineZ Wiki Editors")
        logger.info(" ")
        logger.info("Version: 0.0.42")
        logger.info(" ")
        logger.info("[==============================================]")

        FabricLoader.getInstance().getModContainer("minez-help-menu").ifPresent { container ->
            val packs = listOf("default", "zombies", "dungshelper")

            for (pack in packs) {
                ResourceManagerHelper.registerBuiltinResourcePack(
                    Identifier.of("minez-help-menu", pack),
                    container,
                    ResourcePackActivationType.NORMAL
                )
            }
        }
    }
}