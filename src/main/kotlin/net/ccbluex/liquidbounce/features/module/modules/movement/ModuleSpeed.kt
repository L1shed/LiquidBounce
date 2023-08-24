/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2023 CCBlueX
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
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.config.Choice
import net.ccbluex.liquidbounce.config.ChoiceConfigurable
import net.ccbluex.liquidbounce.event.PlayerMoveEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.repeatable
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.ModuleCriticals
import net.ccbluex.liquidbounce.utils.client.timer
import net.ccbluex.liquidbounce.utils.entity.*
import net.minecraft.entity.MovementType

/**
 * Speed module
 *
 * Allows you to move faster.
 */

object ModuleSpeed : Module("Speed", Category.MOVEMENT) {

    private val modes = choices(
        "Mode", SpeedYPort, arrayOf(
            Verus,
            SpeedYPort,
            LegitHop,
            Custom,
        )
    )

    /**
     * @anticheat Verus
     * @anticheatVersion b3882
     * @testedOn eu.anticheat-test.com
     */

    private object Verus : Choice("Verus") {

        //b3882
        override val parent: ChoiceConfigurable
            get() = modes

        val repeatable = repeatable {
            if (player.isOnGround && player.moving) {
                player.jump()
                player.velocity.x *= 1.1
                player.velocity.z *= 1.1
            }
        }

        val moveHandler = handler<PlayerMoveEvent> { event ->
            // Might just strafe when player controls itself
            if (event.type == MovementType.SELF && player.moving) {
                val movement = event.movement
                movement.strafe(player.directionYaw, strength = 1.0)
            }
        }

        val timerRepeatable = repeatable {
            mc.timer.timerSpeed = 2f
            wait { 1 }
            mc.timer.timerSpeed = 1f
            wait { 100 }
        }

        override fun disable() {
            mc.timer.timerSpeed = 1f
        }
    }

    private object SpeedYPort : Choice("YPort") {

        override val parent: ChoiceConfigurable
            get() = modes

        val repeatable = repeatable {
            if (player.isOnGround && player.moving) {
                player.strafe(speed = 0.4)
                player.upwards(0.42f)
                wait(1)
                player.downwards(-1f)
            }
        }

    }

    private object LegitHop : Choice("LegitHop") {

        override val parent: ChoiceConfigurable
            get() = modes

        private val optimizeForCriticals by boolean("OptimizeForCriticals", true)

        val repeatable = repeatable {
            if (optimizeForCriticals && ModuleCriticals.shouldWaitForJump(0.42f)) {
                return@repeatable
            }

            if (player.isOnGround && player.moving) {
                player.jump()
            }
        }

    }

    private object Custom : Choice("Custom") {
        override val parent: ChoiceConfigurable
            get() = modes

        private val horizontalSpeed by float("HorizontalSpeed", 1f, 0.1f..10f)
        private val resetHorizontalSpeed by boolean("ResetHorizontalSpeed", true)
        private val customStrafe by boolean("CustomStrafe", false)
        private val strafe by float("Strafe", 1f, 0.1f..10f)
        private val verticalSpeed by float("VerticalSpeed", 0.42f, 0.1f..3f)
        private val resetVerticalSpeed by boolean("ResetVerticalSpeed", true)
        private val timerSpeed by float("TimerSpeed", 1f, 0.1f..10f)

        val repeatable = repeatable {
            if (player.moving) {
                mc.timer.timerSpeed = timerSpeed

                when {
                    player.isOnGround -> {
                        player.strafe(speed = horizontalSpeed.toDouble())
                        player.velocity.y = verticalSpeed.toDouble()
                    }

                    customStrafe -> player.strafe(speed = strafe.toDouble())
                    else -> player.strafe()
                }
            } else {
                mc.timer.timerSpeed - 1f
            }
        }

        override fun enable() {
            if (resetHorizontalSpeed) {
                player.velocity.x = 0.0
                player.velocity.z = 0.0
            }

            if (resetVerticalSpeed) player.velocity.y = 0.0
        }

        override fun disable() {
            mc.timer.timerSpeed = 1f
        }
    }

}