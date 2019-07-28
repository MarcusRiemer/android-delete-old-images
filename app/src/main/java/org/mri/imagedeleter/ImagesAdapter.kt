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
import java.util.*
import kotlin.collections.ArrayList

class ImagesAdapter(private val context: Context) : RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {

    private var items : List<ImageItem> = ArrayList<ImageItem>()

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

    fun getCameraImages(context: Context, criteria: DeletionCriteria): List<ImageItem> {
        val camera_image_bucket_name = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera"
        val camera_image_bucket_id = getBucketId(camera_image_bucket_name)

        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val selection = MediaStore.Images.Media.BUCKET_ID + " = ?"
        val selectionArgs = arrayOf(camera_image_bucket_id)
        val cursor: Cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )
        val result = ArrayList<ImageItem>(cursor.count)
        if (cursor.moveToFirst()) {
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            do {
                val data = cursor.getString(dataColumn)
                result.add(ImageItem(data))
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
        fun bind(item: ImageItem) {
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