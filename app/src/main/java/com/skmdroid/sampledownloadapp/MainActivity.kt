package com.skmdroid.sampledownloadapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.skmdroid.sampledownloadapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), DownloadResultReceiver.Receiver {

    companion object {
        const val REQUEST_CODE: Int = 1
    }

    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.downloadButton.setOnClickListener {
            if (isStoragePermissionGranted()) {
                downloadFile()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            && requestCode == REQUEST_CODE
        ) {
            downloadFile()
        }
    }

    private fun downloadFile() {
        val mReceiver = DownloadResultReceiver(Handler())
        mReceiver.setReceiver(this)
        val intent = Intent(Intent.ACTION_SYNC, null, this, DownloadService::class.java)
        val url = binding.urlEditText.text.toString()
        if (url.isNotEmpty()) {
            val getUrl = validateUrl(url)
            intent.putExtra(
                "url", getUrl
            )
            intent.putExtra("receiver", mReceiver)
            intent.putExtra("requestId", 101)
            startService(intent)
        } else {
            Toast.makeText(applicationContext, "Url cannot be empty", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun validateUrl(urlText: String): String {
        val urlData = urlText.trim().replace(" ", "")
        if (urlData.startsWith("http://") || urlData.startsWith("https://")) {
            return urlText
        } else {
            return "https://$urlText"
        }
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        when (resultCode) {
            DownloadService.STATUS_STARTED -> {
                resultData?.let {
                    binding.resultTextView.append("Downloading file: file${resultData?.get(Intent.EXTRA_TEXT)}\n")
                }

            }
            DownloadService.STATUS_FINISHED -> {
                val result = resultData?.getString("result")
                binding.resultTextView.append("File has been successfully downloaded to: $result\n")
            }
            DownloadService.STATUS_ERROR -> {
                binding.resultTextView.append(
                    "Error while downloading data:\n ${
                        resultData?.getString(
                            Intent.EXTRA_TEXT
                        )
                    }\n"
                )
            }
        }
    }

    private fun isStoragePermissionGranted() =
        checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
}