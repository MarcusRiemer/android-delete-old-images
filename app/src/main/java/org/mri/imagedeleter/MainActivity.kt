package org.mri.imagedeleter

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.app.DatePickerDialog
import android.text.format.DateFormat
import android.widget.EditText
import java.util.*


class MainActivity : AppCompatActivity() {


    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 12

    private var editTextImagesSince: EditText? = null

    private val imagesAdapter = ImagesAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to read the contacts
            }

            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
            );

            // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
            // app-defined int constant that should be quite unique

            return;
        }

        imagesAdapter.refreshCameraImages()

        editTextImagesSince = findViewById<EditText>(R.id.edit_text_date)

        val sharedBeginCalendar = Calendar.getInstance()
        updateBeforeDate(sharedBeginCalendar)

        val date = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

            sharedBeginCalendar.set(Calendar.YEAR, year)
            sharedBeginCalendar.set(Calendar.MONTH, monthOfYear)
            sharedBeginCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateBeforeDate(sharedBeginCalendar)
        }

        editTextImagesSince!!.setOnClickListener {
            DatePickerDialog(
                this@MainActivity, date, sharedBeginCalendar
                    .get(Calendar.YEAR), sharedBeginCalendar.get(Calendar.MONTH),
                sharedBeginCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.images_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = imagesAdapter
    }

    private fun updateBeforeDate(c: Calendar) {
        val format = DateFormat.getDateFormat(this)
        editTextImagesSince!!.setText(format.format(c.getTime()))

        imagesAdapter.updateSelection(Date(c.timeInMillis))
    }
}
