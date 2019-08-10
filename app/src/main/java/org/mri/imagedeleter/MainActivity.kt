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

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
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
 * Handles the UI for the actual deletion selection. If this were a real app instead of a little utility it should
 * probably use fancy things like fragments, dependency injection or observables. But because the scope of this tool
 * is so limited I don't actually see a benefit in bringing in the big guns.
 */
class MainActivity : AppCompatActivity() {
    private var editTextImagesSince: EditText? = null

    // The images that are about to be deleted
    private val deletionItemsAdapter = DeletionItemsAdapter(this)

    // The criteria to apply when deleting
    private val deletionCriteria = DeletionCriteria.default()

    /**
     * Checking permissions and wiring up events.
     */
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

        // React to deleting
        val btnDelete = findViewById<Button>(R.id.btn_delete)
        btnDelete.setOnClickListener {
            val res = this@MainActivity.deletionItemsAdapter.deleteSelection()
            if (res.numDeleted() > 0) {
                val d = deleteConfirmationDialog(res)
                d.show()
            } else {
                val t = Toast.makeText(this, getString(R.string.toast_empty_delete), Toast.LENGTH_LONG)
                t.show()
            }
        }

        // React to changes in the types of item to select
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

                    this@MainActivity.deletionItemsAdapter.refreshCameraImages(this@MainActivity.deletionCriteria)
                }

            }


        // Update deletion date
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

        // Populate the shown images
        val recyclerView = findViewById<RecyclerView>(R.id.images_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = deletionItemsAdapter
    }

    override fun onResume() {
        super.onResume()

        deletionItemsAdapter.refreshCameraImages(deletionCriteria)
    }

    private fun updateBeforeDate() {
        val format = DateFormat.getDateFormat(this)
        editTextImagesSince!!.setText(format.format(deletionCriteria.deleteBefore.time))

        deletionItemsAdapter.updateSelection(deletionCriteria)
    }

    private fun deleteConfirmationDialog(res: DeletionProcess) = AlertDialog.Builder(this)
        .setTitle(getString(R.string.dlg_really_delete_title, res.numDeleted(), res.formattedTotalSize()))
        .setPositiveButton(
            R.string.dlg_really_delete_positive
        ) { dlg: DialogInterface, _: Int ->
            val numDeleted = res.actuallyDelete()
            dlg.dismiss()

            Toast.makeText(this, getString(R.string.toast_num_deleted, numDeleted), Toast.LENGTH_LONG)
                .show()
        }
        .setNegativeButton(
            R.string.dlg_really_delete_negative
        ) { dlg: DialogInterface, _: Int ->
            dlg.cancel()
        }
        .create()

}
