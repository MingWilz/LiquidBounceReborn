/*
 * Originally made by LuckySynx#2351 and GOOSE(RIP)
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.world.WorldSettings

@ModuleInfo(name = "BackTrack", description = "Suspends all movement packets.", category = ModuleCategory.COMBAT)
class BackTrack : Module() {

    var fakePlayer: EntityOtherPlayerMP? = null
    private val aura =
        BoolValue("Aura", false)
    private val pulseDelayValue =
        IntegerValue("PulseDelay", 1000, 5, 2000)
    private val intavetest =
        BoolValue("IntaveTest", false)
    private val intavetesthurttime =
        IntegerValue("Value", 5, 0, 100)
    private val pulseTimer = MSTimer()
    var currentTarget: EntityLivingBase? = null
    private var shown = false

    val killaura = LiquidBounce.moduleManager.getModule(KillAura::class.java) as KillAura

    override fun onDisable() {
        removeFakePlayer()
    }

    private fun removeFakePlayer() {
        try {
            currentTarget = null
            (mc.theWorld ?: return).removeEntityFromWorld((fakePlayer ?: return).entityId) ?: return
            fakePlayer = null
        } catch (t: NullPointerException) {
            ClientUtils.getLogger().error("Failed removing fakePlayer $t")
        }
    }

    private fun attackEntity(entity: EntityLivingBase) {
        val thePlayer = mc.thePlayer ?: return
        thePlayer.swingItem()
        mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))
        if (mc.playerController.currentGameType != WorldSettings.GameType.SPECTATOR)
            thePlayer.attackTargetEntityWithCurrentItem(entity)
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (fakePlayer == null) {
            currentTarget = event.targetEntity as EntityLivingBase?
            shown = false
        } else {
            if (event.targetEntity == fakePlayer) {
                attackEntity(currentTarget ?: return)
            } else {
                fakePlayer = null
                currentTarget = event.targetEntity as EntityLivingBase?
                shown = false
            }
        }
    }

    @EventTarget
    fun onUpdate(@Suppress("UNUSED_PARAMETER") event: UpdateEvent?) {
        if (aura.get() && !killaura.state) {
            currentTarget = null
            (mc.theWorld ?: return).removeEntityFromWorld((fakePlayer ?: return).entityId) ?: return
            fakePlayer = null
        }
        if (mc.thePlayer == null)
            return
        if (fakePlayer != null && EntityUtils.isRendered(fakePlayer ?: return) && ((currentTarget ?: return).isDead || !EntityUtils.isRendered(
                currentTarget ?: return
            ))
        ) {
            removeFakePlayer()
        }
        if (currentTarget != null && fakePlayer != null) {
            (fakePlayer ?: return).health = (currentTarget ?: return).health
            val indices = (0..4).toList().toIntArray()
            for (index in indices) {
                val equipmentInSlot = (currentTarget ?: return).getEquipmentInSlot(index) ?: continue
                (fakePlayer ?: return).setCurrentItemOrArmor(index, equipmentInSlot)
            }
        }
        if (intavetest.get() && mc.thePlayer.ticksExisted % intavetesthurttime.get() == 0) {
            if (fakePlayer != null) {
                (fakePlayer ?: return).rotationYawHead = (currentTarget ?: return).rotationYawHead
                (fakePlayer ?: return).renderYawOffset = (currentTarget ?: return).renderYawOffset
                (fakePlayer ?: return).copyLocationAndAnglesFrom(currentTarget ?: return)
                (fakePlayer ?: return).rotationYawHead = (currentTarget ?: return).rotationYawHead
            }
            pulseTimer.reset()
        }else   if (!intavetest.get() && pulseTimer.hasTimePassed(pulseDelayValue.get().toLong())) {
            if (fakePlayer != null) {
                (fakePlayer ?: return).rotationYawHead = (currentTarget ?: return).rotationYawHead
                (fakePlayer ?: return).renderYawOffset = (currentTarget ?: return).renderYawOffset
                (fakePlayer ?: return).copyLocationAndAnglesFrom(currentTarget ?: return)
                (fakePlayer ?: return).rotationYawHead = (currentTarget ?: return).rotationYawHead
            }
            pulseTimer.reset()
        }

        if (!shown && currentTarget != null && (currentTarget ?: return).uniqueID != null && mc.netHandler.getPlayerInfo(
                (currentTarget ?: return).uniqueID ?: return
            ) != null && mc.netHandler.getPlayerInfo((currentTarget ?: return).uniqueID ?: return).gameProfile != null
        ) {
            val faker = EntityOtherPlayerMP(
                mc.theWorld ?: return,
                mc.netHandler.getPlayerInfo((currentTarget ?: return).uniqueID ?: return).gameProfile ?: return
            )

            faker.rotationYawHead = (currentTarget ?: return).rotationYawHead
            faker.renderYawOffset = (currentTarget ?: return).renderYawOffset
            faker.copyLocationAndAnglesFrom(currentTarget ?: return)
            faker.rotationYawHead = (currentTarget ?: return).rotationYawHead
            faker.health = (currentTarget ?: return).health
            val indices = (0..4).toList().toIntArray()
            for (index in indices) {
                val equipmentInSlot = (currentTarget ?: return).getEquipmentInSlot(index) ?: continue
                faker.setCurrentItemOrArmor(index, equipmentInSlot)
            }
            (mc.theWorld ?: return).addEntityToWorld(-1337, faker)

            fakePlayer = faker
            shown = true
        }
    }
}