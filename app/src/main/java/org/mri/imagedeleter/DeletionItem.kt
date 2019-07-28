package org.mri.imagedeleter

import android.content.Context
import android.graphics.Color
import android.text.format.DateFormat
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifSubIFDDirectory
import java.io.File
import java.text.DecimalFormat
import java.util.*

/**
 * A single item that may or may not be deleted later. Provides all the relevant UI and
 * computed data that is required for the actual filtering of deleted items.
 */
open class DeletionItem(
    path: String,
    val mediaType: Type
) {
    enum class Type {
        IMAGE,
        VIDEO
    }

    companion object {
        fun create(path: String, mediaType: Type): DeletionItem {
            if (mediaType == Type.VIDEO) {
                return DeletionItem(path, mediaType)
            } else {
                return DeletionItem(path, mediaType)
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
        selected = (creationDate().before(criteria.deleteBefore))
    }
}

class VideoDeletionItem(path: String) : DeletionItem(
    path,
    Type.VIDEO
)

class ImageDeletionItem(path: String) : DeletionItem(
    path,
    Type.VIDEO
)

