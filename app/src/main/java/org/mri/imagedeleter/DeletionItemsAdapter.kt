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

class DeletionItemsAdapter(private val context: Context) : RecyclerView.Adapter<DeletionItemsAdapter.ViewHolder>() {

    private var items: List<DeletionItem> = ArrayList<DeletionItem>()

    fun refreshCameraImages(criteria: DeletionCriteria) {
        this.items = getCameraImages(context, criteria)
        notifyDataSetChanged()

        Log.i(DeletionItemsAdapter::class.java.name, "Refreshed camera images, now showing ${items.size} items")
    }

    /**
     * Matches code in MediaProvider.computeBucketValues. Should be a common
     * function.
     */
    fun getBucketId(path: String): String {
        return path.toLowerCase().hashCode().toString()
    }

    private fun selectionTypes(criteria: DeletionCriteria): String {
        val img = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
        val vid = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO

        return when {
            criteria.itemTypes == DeletionItemTypes.IMAGE_AND_VIDEO -> "($img OR $vid)"
            criteria.itemTypes == DeletionItemTypes.IMAGE_ONLY -> img
            criteria.itemTypes == DeletionItemTypes.VIDEO_ONLY -> vid
            else -> throw RuntimeException("Unknown deletion item type: ${criteria.itemTypes}")
        }
    }


    private val contentUri = MediaStore.Files.getContentUri("external")

    fun getCameraImages(context: Context, criteria: DeletionCriteria): List<DeletionItem> {
        val camera_image_bucket_name = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera"
        val camera_image_bucket_id = getBucketId(camera_image_bucket_name)

        // Get relevant columns for use later.
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.TITLE,
            MediaStore.Images.Thumbnails.DATA,
            MediaStore.Video.Thumbnails.DATA
        )

        // Return only video and image metadata.
        val selectionBucket = MediaStore.Images.Media.BUCKET_ID + " = ?"
        val selection = "$selectionBucket AND ${selectionTypes(criteria)}"
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
                val id = cursor.getInt(idxId)
                val path = cursor.getString(idxFilePath)
                val isImage = cursor.getInt(idxMediaType) == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                val type = if (isImage) DeletionItem.Type.IMAGE else DeletionItem.Type.VIDEO

                val idxThumbPath = if (isImage)
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA)
                else
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA)
                val thumbPath = cursor.getString(idxThumbPath)

                val item = DeletionItem(id, path, thumbPath, type)

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.image_item, null))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    fun updateSelection(criteria: DeletionCriteria) {
        items.forEach {
            it.updateSelection(criteria)
        }
        notifyDataSetChanged()

        Log.i(DeletionItemsAdapter::class.java.name, "Updated selection, showing ${items.size} items")
    }

    fun deleteSelection(): DeletionResult {
        val partitioned = items.partition { it.selected }
        val toDelete = partitioned.first
        val remaining = partitioned.second

        return (DeletionResult(contentUri, context.contentResolver, toDelete) {
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