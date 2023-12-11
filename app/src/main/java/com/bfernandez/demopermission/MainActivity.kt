package com.bfernandez.demopermission

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bfernandez.demopermission.databinding.ActivityMainBinding
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE = 300
        val PERMISSIONS = arrayOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    private fun initUI() {
        initButtons()
    }

    private fun initButtons() {
        binding.btnPDF.setOnClickListener { tryDoSomething() }
    }

    private fun tryDoSomething() {
//        showMessage()
        if (checkUserPermission()) generatePDF()
        else requestPermission()
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE)
        }
    }

    private val storageActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) showMessage()
                else showMessageDenied()
            }
        }

    private fun checkUserPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val permissionWriteResult = ContextCompat.checkSelfPermission(this, PERMISSIONS[0])
            val permissionReadResult = ContextCompat.checkSelfPermission(this, PERMISSIONS[1])
            permissionWriteResult == PackageManager.PERMISSION_GRANTED && permissionReadResult == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                showMessage()
            } else showMessageDenied()
        }
    }

    private fun showMessage() {
        Toast.makeText(this, "Has permissions", Toast.LENGTH_SHORT).show()
    }

    private fun showMessageDenied() {
        Toast.makeText(this, "NO HAS permissions", Toast.LENGTH_SHORT).show()
    }

    private fun generatePDF() {
        /* THIS WAY TO ACCESS A EXTERNAL FILES DIR IS TO GET A PATH DOWNLOADS DIRECTORY TO APP
        ContextCompat.getExternalFilesDirs(this, Environment.DIRECTORY_DOWNLOADS)[0].toString() */
        try {
            //  THIS WAY TO ACCESS A EXTERNAL FILES DIR IS TO GET A PUBLIC PATH DOWNLOADS DIRECTORY
            val filePath = "${Environment.getExternalStorageDirectory().path}/${Environment.DIRECTORY_DOWNLOADS}/test.pdf"
            Log.i("FILE", filePath)

            val doc = Document()
            PdfWriter.getInstance(doc, FileOutputStream(filePath))

            doc.open()

            val exampleTitle = Paragraph("THIS IS AN EXAMPLE TITLE...!!!")
            doc.add(exampleTitle)

            doc.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}