package org.mri.imagedeleter

data class DeletionResult (
    private val items: List<DeletionItem>
) {
    fun totalSize() = items.map { item -> item.fileSize() }
        .sum()

    fun numDeleted() = items.size

    fun sizeDeleted() = readableFileSize(totalSize())
}