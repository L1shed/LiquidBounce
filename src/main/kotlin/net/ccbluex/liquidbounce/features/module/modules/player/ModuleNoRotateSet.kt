/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2024 CCBlueX
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

import net.ccbluex.liquidbounce.config.types.Choice
import net.ccbluex.liquidbounce.config.types.ChoiceConfigurable
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.aiming.RotationsConfigurable

/**
 * NoRotateSet module.
 *
 * Prevents the server from rotating your head.
 */
object ModuleNoRotateSet : Module("NoRotateSet", Category.PLAYER) {
    val mode = choices(
        "Mode", SilentAccept, arrayOf(
            SilentAccept, ResetRotation
        )
    ).apply { tagBy(this) }

    object ResetRotation : Choice("ResetRotation") {
        override val parent: ChoiceConfigurable<Choice>
            get() = mode

        val rotationsConfigurable = tree(RotationsConfigurable(this))
    }

    object SilentAccept : Choice("SilentAccept") {
        override val parent: ChoiceConfigurable<Choice>
            get() = mode
    }
}
