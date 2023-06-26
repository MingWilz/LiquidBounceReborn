/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minecraft.client.gui.GuiGameOver

@ModuleInfo(name = "Ghost", description = "Allows you to walk around and interact with your environment after dying.", category = ModuleCategory.WORLD)
class Ghost : Module() {

    private var isGhost = false

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.currentScreen is GuiGameOver) {
            mc.displayGuiScreen(null)
            mc.thePlayer.isDead = false
            mc.thePlayer.health = 20F
            mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
            isGhost = true

            ClientUtils.displayChatMessage("§cYou are now a ghost.")
        }
    }

    override fun onDisable() {
        if (isGhost) {
            mc.thePlayer.respawnPlayer()
            isGhost = false
        }
    }

}