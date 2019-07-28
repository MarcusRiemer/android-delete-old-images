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