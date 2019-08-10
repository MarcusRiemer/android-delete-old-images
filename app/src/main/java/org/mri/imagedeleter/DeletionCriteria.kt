/*
 * This file is part of the Android Image Deleter.
 *
 * Android Image Deleter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Android Image Deleter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Android Image Deleter.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.mri.imagedeleter

import java.util.*

enum class DeletionItemTypes {
    IMAGE_AND_VIDEO,
    IMAGE_ONLY,
    VIDEO_ONLY
}

/**
 * Groups together the various criteria that must be met for an image to be deleted.
 */
data class DeletionCriteria(
    var deleteBefore: Date,
    var itemTypes: DeletionItemTypes
) {
    companion object {
        /**
         * A default criteria object that deletes everything that is a few days old.
         */
        fun default(): DeletionCriteria {
            val initialDayMidnight = Calendar.getInstance()
            initialDayMidnight.set(Calendar.HOUR, 0)
            initialDayMidnight.set(Calendar.MINUTE, 0)
            initialDayMidnight.set(Calendar.SECOND, 0)
            initialDayMidnight.set(Calendar.MILLISECOND, 0)

            initialDayMidnight.add(Calendar.DAY_OF_MONTH, -3)

            return (DeletionCriteria(
                Date(initialDayMidnight.timeInMillis),
                DeletionItemTypes.IMAGE_AND_VIDEO
            ))
        }
    }
}