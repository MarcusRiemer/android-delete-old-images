package org.mri.imagedeleter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.text.format.DateFormat
import java.io.File
import java.util.*

/**
 * A single item that may or may not be deleted later. Provides all the relevant UI and
 * computed data that is required for the actual filtering of deleted items.
 */
class DeletionItem(
    path: String,
    private val thumbPath: String,
    val mediaType: Type
) {
    enum class Type {
        IMAGE,
        VIDEO
    }

    companion object {
        fun create(path: String, thumbPath: String, mediaType: Type): DeletionItem {
            if (mediaType == Type.VIDEO) {
                return DeletionItem(path, thumbPath, mediaType)
            } else {
                return DeletionItem(path, thumbPath, mediaType)
            }
        }
    }

    val file = File(path)

    var selected = true
        get() = field

    fun creationDate() = Date(file.lastModified())

    fun formattedDate(ctx: Context): String = DateFormat.getDateFormat(ctx).format(creationDate())

    fun fileSize() = file.length()

    val formattedSize = readableFileSize(fileSize())

    fun backgroundColor() = if (selected) Color.RED else Color.TRANSPARENT

    fun updateSelection(criteria: DeletionCriteria) {
        selected = isMatchingMediaType(criteria.itemTypes) && (creationDate().before(criteria.deleteBefore))
    }

    fun isMatchingMediaType(request: DeletionItemTypes) =
        request == DeletionItemTypes.IMAGE_AND_VIDEO
                || (mediaType == Type.IMAGE && request == DeletionItemTypes.IMAGE_ONLY)
                || (mediaType == Type.VIDEO && request == DeletionItemTypes.VIDEO_ONLY)

    fun thumbnail() = BitmapFactory.decodeFile(thumbPath);
}

