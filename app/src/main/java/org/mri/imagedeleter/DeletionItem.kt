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

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.text.format.DateFormat
import java.io.File
import java.util.*

/**
 * A single item that may or may not be deleted later. Provides all the relevant UI and
 * computed data that is required for the actual filtering of deleted items.
 */
class DeletionItem(
    val id: Long,
    path: String,
    private val thumbPath: String,
    private val contentResolver: ContentResolver,
    val mediaType: Type
) {
    /**
     * The type of item that would be deleted
     */
    enum class Type {
        IMAGE,
        VIDEO
    }

    // The backing file instance
    val file = File(path)

    // True, if this item is scheduled for deletion
    var selected = true
        get() = field

    /**
     * The supposed creation date of this item. For some reason Android doesn't seem to expose this date in the
     * filesystem so we just go with the next nearest guess.
     */
    fun creationDate() = Date(file.lastModified())

    /**
     * The date to show in the UI
     */
    fun formattedDate(ctx: Context): String = DateFormat.getDateFormat(ctx).format(creationDate())

    /**
     * The file size in bytes
     */
    fun fileSize() = file.length()

    /**
     * The size that should be shown in the UI
     */
    val formattedSize = readableFileSize(fileSize())

    /**
     * The colour that should be shown in the UI
     */
    fun backgroundColor() = if (selected) Color.RED else Color.TRANSPARENT

    /**
     * Select or deselect this item based on the given criteria
     */
    fun updateSelection(criteria: DeletionCriteria) {
        selected = isMatchingMediaType(criteria.itemTypes) && (creationDate().before(criteria.deleteBefore))
    }

    /**
     * Predicate to decide whether this item matches the given deletion type.
     */
    fun isMatchingMediaType(request: DeletionItemTypes) =
        request == DeletionItemTypes.IMAGE_AND_VIDEO
                || (mediaType == Type.IMAGE && request == DeletionItemTypes.IMAGE_ONLY)
                || (mediaType == Type.VIDEO && request == DeletionItemTypes.VIDEO_ONLY)

    /**
     * A thumbnail for this deletion item.
     */
    fun thumbnail(): Bitmap = if (mediaType == Type.VIDEO)
        ThumbnailUtils.createVideoThumbnail(file.absolutePath, MediaStore.Images.Thumbnails.MINI_KIND)
    else
        MediaStore.Images.Thumbnails.getThumbnail(
            contentResolver,
            id,
            MediaStore.Images.Thumbnails.MINI_KIND,
            BitmapFactory.Options()
        )
}

