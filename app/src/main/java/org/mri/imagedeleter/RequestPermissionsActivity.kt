package org.mri.imagedeleter

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class RequestPermissionsActivity : AppCompatActivity() {
    companion object {
        private const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 12

        fun hasPermissions(a: Activity) =
            a.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }


    private fun ensurePermissions() {
        // Should we show an explanation?
        if (shouldShowRequestPermissionRationale(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            // Explain to the user why we need to read the contacts
        }

        requestPermissions(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
        );
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_permissions)

        val btnGrantPermission = findViewById<Button>(R.id.btn_grant_permission)
        btnGrantPermission.setOnClickListener {
            this@RequestPermissionsActivity.ensurePermissions();

            if (hasPermissions(this@RequestPermissionsActivity)) {
                this@RequestPermissionsActivity.startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}
