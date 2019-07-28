package org.mri.imagedeleter

data class DeletionResult(
    private val items: List<DeletionItem>,
    private val deletionCallback: (() -> Unit)
) {
    fun totalSize() = items.map { item -> item.fileSize() }
        .sum()

    fun numDeleted() = items.size

    fun sizeDeleted() = readableFileSize(totalSize())

    fun actuallyDelete() {
        items.forEach { item ->
            item.file.delete()
        }

        deletionCallback()
    }
}