package org.mri.imagedeleter

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore


data class DeletionResult(
    private val uri: Uri,
    private val contentResolver: ContentResolver,
    private val items: List<DeletionItem>,
    private val deletionCallback: (() -> Unit)
) {
    fun totalSize() = items.map { item -> item.fileSize() }
        .sum()

    fun numDeleted() = items.size

    fun sizeDeleted() = readableFileSize(totalSize())

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