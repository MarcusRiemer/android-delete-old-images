package org.mri.imagedeleter

import android.content.Context
import android.database.Cursor
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.collections.ArrayList

class ImagesAdapter(private val context: Context) : RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {

    private var items: List<DeletionItem> = ArrayList<DeletionItem>()

    fun refreshCameraImages(criteria: DeletionCriteria) {
        this.items = getCameraImages(context, criteria)
    }

    /**
     * Matches code in MediaProvider.computeBucketValues. Should be a common
     * function.
     */
    fun getBucketId(path: String): String {
        return path.toLowerCase().hashCode().toString()
    }

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
            MediaStore.Files.FileColumns.TITLE
        )

        // Return only video and image metadata.
        val selectionTypes = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
        val selectionBucket = MediaStore.Images.Media.BUCKET_ID + " = ?"
        val selection = "$selectionBucket AND ($selectionTypes)"
        val selectionArgs = arrayOf(camera_image_bucket_id)
        val cursor: Cursor = context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            null
        )

        val result = ArrayList<DeletionItem>(cursor.count)
        if (cursor.moveToFirst()) {
            val idxFilePath = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val idxMediaType = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            do {
                val path = cursor.getString(idxFilePath)
                val isImage = cursor.getInt(idxMediaType) == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                val type = if (isImage) DeletionItem.Type.IMAGE else DeletionItem.Type.VIDEO
                result.add(DeletionItem(path, type))
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
    }

    fun deleteSelection(): DeletionResult {
        val partitioned = items.partition { it.selected }
        val toDelete = partitioned.first
        items = partitioned.second

        // toDelete.forEach { it.file.delete() }

        notifyDataSetChanged()

        return (DeletionResult(toDelete))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: DeletionItem) {
            val imageView = itemView.findViewById<ImageView>(R.id.image_item_view)
            val imageViewDate = itemView.findViewById<TextView>(R.id.image_date)
            val imageViewSize = itemView.findViewById<TextView>(R.id.image_size)

            val myBitmap = BitmapFactory.decodeFile(item.file.absolutePath)
            imageView.setImageBitmap(myBitmap)

            imageViewDate.text = item.formattedDate(imageView.context)
            imageViewSize.text = item.formattedSize
            itemView.setBackgroundColor(item.backgroundColor())

        }
    }
}