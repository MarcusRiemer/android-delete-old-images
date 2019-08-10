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

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore

/**
 * A proposed set of items that could be deleted.
 */
data class DeletionProcess(
    private val uri: Uri,
    private val contentResolver: ContentResolver,
    private val items: List<DeletionItem>,
    private val deletionCallback: (() -> Unit)
) {
    /**
     * The total size that would be freed by actually deleting these items.
     */
    fun totalSize() = items.map { item -> item.fileSize() }
        .sum()

    /**
     * The size to show in the UI
     */
    fun formattedTotalSize() = readableFileSize(totalSize())

    /**
     * The number of items that would be deleted.
     */
    fun numDeleted() = items.size

    /**
     * Delete files from disk and also tell the content provider about these deletions.
     */
    fun actuallyDelete(): Int {
        val operations = ArrayList<ContentProviderOperation>()
        var operation: ContentProviderOperation

        // Delete files and build query to remove items from content provider
        for (item in items) {
            if (item.file.delete()) {
                operation = ContentProviderOperation
                    .newDelete(uri)
                    .withSelection(MediaStore.Files.FileColumns._ID + " = ?", arrayOf(item.id.toString()))
                    .build()

                operations.add(operation)
            }
        }

        contentResolver.applyBatch(uri.authority, operations)

        deletionCallback()
        return operations.size
    }
}