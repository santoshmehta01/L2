package com.example.l2

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.l2.databinding.ActivityMainBinding
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog

private const val FILE_RC = 1
private const val PERMISSION_RC = 51
private const val URL = "http://learn-industry.com/slideshows"

@SuppressLint("SetJavaScriptEnabled")
class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private lateinit var binding: ActivityMainBinding
    private var filePath: String? = null
    private var fileCallBack: ValueCallback<Array<Uri>>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        supportActionBar?.hide()



        lifecycleScope.launchWhenStarted {
            setupWebView()
            if (!hasRequiredPermission()) requestPermission()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(this).build().show()
        } else {
            requestPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Toast.makeText(this, "Permission granted!!", Toast.LENGTH_LONG).show()
    }


    private fun hasRequiredPermission() = EasyPermissions.hasPermissions(
        this,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.CAMERA
    )

    private fun requestPermission() {
        EasyPermissions.requestPermissions(
            this,
            "This application may not work properly without requested permissions!!",
            PERMISSION_RC,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.RECORD_AUDIO
        )
    }


    private fun setupWebView() {
        binding.webView.apply {
            this.canGoBack()
            this.loadUrl(URL)
            this.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    view?.loadUrl(request?.url.toString())
                    return false
                }
            }
            this.settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                allowUniversalAccessFromFileURLs = true
                allowFileAccessFromFileURLs = true
                javaScriptCanOpenWindowsAutomatically = true
            }
            this.webChromeClient = object : WebChromeClient() {
                @SuppressLint("QueryPermissionsNeeded")
                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    if (fileCallBack != null) {
                        fileCallBack!!.onReceiveValue(null)
                    }
                    if (!hasRequiredPermission()) {
                        this@MainActivity.requestPermission()
                    }
                    fileCallBack = filePathCallback

                    val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                    contentSelectionIntent.type = "*/*"
                    val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "File Chooser")
                    startActivityForResult(chooserIntent, FILE_RC)
                    return true

                }
            }

        }
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        intent: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, intent)
        var results: Array<Uri>? = null
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == FILE_RC) {
                if (null == fileCallBack) {
                    return
                }
                if (intent == null) {
                    if (filePath != null) {
                        results = arrayOf(Uri.parse(filePath))
                    }
                } else {
                    val dataString = intent.dataString
                    if (dataString != null) {
                        results = arrayOf(Uri.parse(dataString))
                    }
                }
            }
        }
        fileCallBack!!.onReceiveValue(results)
        fileCallBack = null
    }


    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}