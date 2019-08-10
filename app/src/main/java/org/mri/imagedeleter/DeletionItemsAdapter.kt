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

import android.content.Context
import android.database.Cursor
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * A recycler view adpater that queries the Android Media Storage and marks some of the items that exist there for
 * deletion.
 */
class DeletionItemsAdapter(private val context: Context) : RecyclerView.Adapter<DeletionItemsAdapter.ViewHolder>() {

    // Backing list for the items that should be deleted.
    private var items: List<DeletionItem> = ArrayList<DeletionItem>()

    /**
     * Update the shown deletion items, possibly based on new criteria.
     */
    fun refreshCameraImages(criteria: DeletionCriteria) {
        this.items = queryContentResolver(criteria)
        notifyDataSetChanged()

        Log.i(DeletionItemsAdapter::class.java.name, "Refreshed camera images, now showing ${items.size} items")
    }

    /**
     * Matches code in MediaProvider.computeBucketValues.
     */
    fun getBucketId(path: String): String {
        return path.toLowerCase().hashCode().toString()
    }

    /**
     * Builds a partial SQL expression that can be used in a WHERE clause to only select rows of a certain media type.
     */
    private fun querySelectionTypes(criteria: DeletionCriteria): String {
        val img = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
        val vid = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO

        return when {
            criteria.itemTypes == DeletionItemTypes.IMAGE_AND_VIDEO -> "($img OR $vid)"
            criteria.itemTypes == DeletionItemTypes.IMAGE_ONLY -> img
            criteria.itemTypes == DeletionItemTypes.VIDEO_ONLY -> vid
            else -> throw RuntimeException("Unknown deletion item type: ${criteria.itemTypes}")
        }
    }

    /**
     * The place we expect the content to be
     */
    private val contentUri = MediaStore.Files.getContentUri("external")

    /**
     * Queries the MediaStore for media items that match the given criteria.
     */
    fun queryContentResolver(criteria: DeletionCriteria): List<DeletionItem> {
        val camera_image_bucket_name = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera"
        val camera_image_bucket_id = getBucketId(camera_image_bucket_name)

        // Get relevant columns for use later.
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.TITLE,
            MediaStore.Images.Thumbnails.DATA,
            MediaStore.Video.Thumbnails.DATA
        )

        // Return only video and image metadata.
        val selectionBucket = MediaStore.Images.Media.BUCKET_ID + " = ?"
        val selection = "$selectionBucket AND ${querySelectionTypes(criteria)}"
        val selectionArgs = arrayOf(camera_image_bucket_id)
        val sortOrder = MediaStore.Files.FileColumns.DATE_ADDED
        val cursor: Cursor = context.contentResolver.query(
            contentUri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        val result = ArrayList<DeletionItem>(cursor.count)
        if (cursor.moveToFirst()) {
            val idxId = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val idxFilePath = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val idxMediaType = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            do {
                val id = cursor.getLong(idxId)
                val path = cursor.getString(idxFilePath)
                val isImage = cursor.getInt(idxMediaType) == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                val type = if (isImage) DeletionItem.Type.IMAGE else DeletionItem.Type.VIDEO

                val idxThumbPath = if (isImage)
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA)
                else
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA)
                val thumbPath = cursor.getString(idxThumbPath)

                val item = DeletionItem(id, path, thumbPath, context.contentResolver, type)

                // Dont add deletion items that exist in the database but not on disk
                if (item.file.exists()) {
                    item.updateSelection(criteria)
                    result.add(item)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return result
    }

    /**
     * Creates the widget that is used to show a deletion item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.image_item, null))

    /**
     * Updates the UI of a previously instanciated widget.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    /**
     * React to changed deletion criteria, usually a changed date.
     */
    fun updateSelection(criteria: DeletionCriteria) {
        items.forEach {
            it.updateSelection(criteria)
        }
        notifyDataSetChanged()

        Log.i(DeletionItemsAdapter::class.java.name, "Updated selection, showing ${items.size} items")
    }

    /**
     * Prepares deletion of the selected items. The returned instance
     */
    fun deleteSelection(): DeletionProcess {
        val partitioned = items.partition { it.selected }
        val toDelete = partitioned.first
        val remaining = partitioned.second

        return (DeletionProcess(contentUri, context.contentResolver, toDelete) {
            this.items = remaining
            notifyDataSetChanged()
        })
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: DeletionItem) {
            val imageView = itemView.findViewById<ImageView>(R.id.image_item_view)
            val imageViewDate = itemView.findViewById<TextView>(R.id.image_date)
            val imageViewSize = itemView.findViewById<TextView>(R.id.image_size)

            imageView.setImageBitmap(item.thumbnail())

            imageViewDate.text = item.formattedDate(imageView.context)
            imageViewSize.text = item.formattedSize
            itemView.setBackgroundColor(item.backgroundColor())

        }
    }
}