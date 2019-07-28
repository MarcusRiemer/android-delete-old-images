package org.mri.imagedeleter

import java.util.*

enum class DeletionItems {
    IMAGE_AND_VIDEO,
    IMAGE_ONLY,
    VIDEO_ONLY
}

data class DeletionCriteria(
    var deleteBefore: Date,
    var itemTypes: DeletionItems
) {
    companion object {
        fun default() = DeletionCriteria(
            Date(Calendar.getInstance().timeInMillis),
            DeletionItems.IMAGE_AND_VIDEO
        )
    }
}