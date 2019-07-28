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
data class ImageItem(
    val path: String
) {
    val file = File(path)

    var selected = true
        get() = field

    private val metadata = ImageMetadataReader.readMetadata(file)
        .getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)

    fun creationDate() = metadata.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)

    fun formattedDate(ctx: Context): String = DateFormat.getDateFormat(ctx).format(creationDate())

    private fun readableFileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(
            size / Math.pow(
                1024.0,
                digitGroups.toDouble()
            )
        ) + " " + units[digitGroups]
    }

    val formattedSize = readableFileSize(file.length())

    fun backgroundColor() = if (selected) Color.RED else Color.TRANSPARENT

    fun updateSelection(deleteBefore: Date) {
        selected = (creationDate().before(deleteBefore))
    }

}