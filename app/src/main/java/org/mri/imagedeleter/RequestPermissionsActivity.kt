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
        private const val PERMISSION_REQUEST_ID = 12

        fun hasPermissions(a: Activity) =
            a.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    a.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }


    private fun ensurePermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            PERMISSION_REQUEST_ID
        );
    }

    private val btnGrantPermission: Button by lazy {
        findViewById<Button>(R.id.btn_grant_permission)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_permissions)

        btnGrantPermission.setOnClickListener {
            btnGrantPermission.isEnabled = false
            ensurePermissions();
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_ID) {
            if (hasPermissions(this)) {
                this.startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                btnGrantPermission.isEnabled = true
            }
        }
    }
}
