/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2025 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.config.types.ToggleableConfigurable
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.tickHandler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.utils.entity.equipmentSlot
import net.minecraft.item.FishingRodItem
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import net.minecraft.util.math.Vec3d

/**
 * AutoFish module
 *
 * Automatically catches fish when using a rod.
 */

object ModuleAutoFish : ClientModule("AutoFish", Category.PLAYER) {

    private val reelDelay by intRange("ReelDelay", 5..8, 0..20, "ticks")

    private object RecastRod : ToggleableConfigurable(this, "RecastRod", true) {
        val delay by intRange("Delay", 15..20, 10..30, "ticks")
    }

    init {
        tree(RecastRod)
    }

    private var caughtFish = false

    override fun disable() {
        caughtFish = false
    }

    @Suppress("unused")
    private val tickHandler = tickHandler {
        if (!caughtFish) {
            return@tickHandler
        }

        for (hand in arrayOf(Hand.MAIN_HAND, Hand.OFF_HAND)) {
            if (player.getEquippedStack(hand.equipmentSlot).item !is FishingRodItem) {
                continue
            }

            waitTicks(reelDelay.random())
            interaction.sendSequencedPacket(world) { sequence ->
                PlayerInteractItemC2SPacket(hand, sequence, player.yaw, player.pitch)
            }

            player.swingHand(hand)

            if (RecastRod.enabled) {
                waitTicks(RecastRod.delay.random())
                interaction.sendSequencedPacket(world) { sequence ->
                    PlayerInteractItemC2SPacket(hand, sequence, player.yaw, player.pitch)
                }
                player.swingHand(hand)
            }
        }

        caughtFish = false
    }

    @Suppress("unused")
    private val packetHandler = handler<PacketEvent> { event ->
        val packet = event.packet
        val fishHook = player.fishHook ?: return@handler
        if (fishHook.isRemoved) {
            return@handler
        }

        if (packet is PlaySoundS2CPacket && packet.sound.value() == SoundEvents.ENTITY_FISHING_BOBBER_SPLASH) {
            val soundPosition = Vec3d(packet.x, packet.y, packet.z)
            val fishHookPosition = fishHook.pos.squaredDistanceTo(soundPosition)

            // From my testing, we should see distances around 0.04 - 0.08 (Paper version 1.21.1-132)
            // so a threshold of 1.0 should be more than enough.
            if (fishHookPosition > 1.0) {
                return@handler
            }

            caughtFish = true
        }
    }

}
