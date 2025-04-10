package team.blombix

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object MineZHelpMenu : ModInitializer {
    private val logger = LoggerFactory.getLogger("minez-help-menu")

    override fun onInitialize() {
        logger.info("[==============================================]")
        logger.info(" ")
        logger.info("MineZ Help Menu Mod")
        logger.info("Author:")
        logger.info("Bubix")
        logger.info(" ")
        logger.info("[==============================================]")
    }
}