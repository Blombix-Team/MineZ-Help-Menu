package team.blombix

import net.fabricmc.api.ModInitializer
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
        logger.info(" ")
        logger.info("Version: 0.0.17")
        logger.info(" ")
        logger.info("[==============================================]")
    }
}