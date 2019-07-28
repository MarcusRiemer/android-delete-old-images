package org.mri.imagedeleter

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * Handles the UI for the actual deletion selection.
 */
class MainActivity : AppCompatActivity() {
    private var editTextImagesSince: EditText? = null

    private val imagesAdapter = ImagesAdapter(this)

    private val deletionCriteria = DeletionCriteria.default()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure we have enough permissions to actually delete something
        if (!RequestPermissionsActivity.hasPermissions(this)) {
            val intent = Intent(this, RequestPermissionsActivity::class.java)
            startActivity(intent)
            finish()
            return;
        }

        setContentView(R.layout.activity_main)

        imagesAdapter.refreshCameraImages(deletionCriteria)

        val btnDelete = findViewById<Button>(R.id.btn_delete)
        btnDelete.setOnClickListener {
            val res = this@MainActivity.imagesAdapter.deleteSelection()
            val toast = if (res.numDeleted() > 0)
                Toast.makeText(
                    this,
                    String.format("Deleted %d items, freed %s", res.numDeleted(), res.sizeDeleted()),
                    Toast.LENGTH_LONG
                )
            else
                Toast.makeText(this, "No items do delete", Toast.LENGTH_LONG)

            toast.show()
        }

        val spnItemType = findViewById<Spinner>(R.id.delete_item_type_selector);

        spnItemType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

                override fun onItemSelected(_ad: AdapterView<*>?, _view: View?, pos: Int, id: Long) {
                    this@MainActivity.deletionCriteria.itemTypes = when {
                        spnItemType.selectedItemPosition == 1 -> DeletionItemTypes.IMAGE_ONLY
                        spnItemType.selectedItemPosition == 2 -> DeletionItemTypes.VIDEO_ONLY
                        else -> DeletionItemTypes.IMAGE_AND_VIDEO
                    }

                    this@MainActivity.imagesAdapter.refreshCameraImages(this@MainActivity.deletionCriteria)
                }

            }


        editTextImagesSince = findViewById<EditText>(R.id.edit_text_date)

        updateBeforeDate()

        val sharedBeginCalendar = Calendar.getInstance()
        sharedBeginCalendar.timeInMillis = deletionCriteria.deleteBefore.time

        val listener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->

            sharedBeginCalendar.set(Calendar.YEAR, year)
            sharedBeginCalendar.set(Calendar.MONTH, monthOfYear)
            sharedBeginCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            deletionCriteria.deleteBefore = Date(sharedBeginCalendar.timeInMillis)
            updateBeforeDate()
        }

        editTextImagesSince!!.setOnClickListener {
            DatePickerDialog(
                this@MainActivity, listener, sharedBeginCalendar
                    .get(Calendar.YEAR), sharedBeginCalendar.get(Calendar.MONTH),
                sharedBeginCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.images_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = imagesAdapter
    }

    override fun onResume() {
        super.onResume()

        imagesAdapter.refreshCameraImages(deletionCriteria)
    }

    private fun updateBeforeDate() {
        val format = DateFormat.getDateFormat(this)
        editTextImagesSince!!.setText(format.format(deletionCriteria.deleteBefore.time))

        imagesAdapter.updateSelection(deletionCriteria)
    }
}
