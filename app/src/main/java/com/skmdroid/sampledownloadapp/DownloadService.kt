package com.skmdroid.sampledownloadapp

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.os.ResultReceiver
import android.util.Log
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random


class DownloadService : Service() {

    companion object {
        const val STATUS_STARTED = 0
        const val STATUS_FINISHED = 1
        const val STATUS_ERROR = 2
        val TAG = DownloadService::class.java.simpleName + "Debug"
    }

    private var disposable = CompositeDisposable()

    private fun backgroundWork(receiver: ResultReceiver, urlText: String, random: Int) {
        Single.create<String> { emitter ->
            try {
                val url = URL(urlText)
                val httpURLConnection = url.openConnection() as HttpURLConnection
                httpURLConnection.requestMethod = "GET"
                httpURLConnection.connect()

                val sdcard = Environment.getExternalStorageDirectory()

                val fileName =
                    "file$random.${httpURLConnection.contentType.substringAfter("/")}"
                val file =
                    File(sdcard, fileName)

                val fileOutput = FileOutputStream(file)
                val inputStream = httpURLConnection.inputStream

                val buffer = ByteArray(1024)
                var bufferLength = 0

                createNotification(urlText, random)

                do {
                    bufferLength = inputStream.read(buffer)
                    Log.d(TAG,"reading...")
                    if (bufferLength > 0) {
                        fileOutput.write(buffer, 0, bufferLength)
                    } else {
                        break
                    }
                } while (true)
                fileOutput.close()
                emitter.onSuccess(file.absolutePath)
            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }.subscribeOn(Schedulers.io())
            .subscribeWith(object : SingleObserver<String> {
                override fun onSubscribe(d: Disposable?) {
                    disposable.add(d)
                    val bundle = Bundle()
                    bundle.putString(Intent.EXTRA_TEXT, random.toString())
                    receiver.send(STATUS_STARTED, bundle)
                }

                override fun onError(e: Throwable?) {
                    val bundle = Bundle()
                    e?.message?.let {
                        bundle.putString(Intent.EXTRA_TEXT, it)
                    } ?: run {
                        bundle.putString(Intent.EXTRA_TEXT, e.toString())
                    }
                    receiver.send(STATUS_ERROR, bundle)
                    stopForeground(true)
                }

                override fun onSuccess(t: String?) {
                    val bundle = Bundle()
                    bundle.putString("result", t)
                    receiver.send(STATUS_FINISHED, bundle)
                    stopForeground(true)
                }
            })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let { localIntent ->
            val url = localIntent.getStringExtra("url")
            if (url.isNotEmpty()) {
                backgroundWork(
                    localIntent.getParcelableExtra("receiver"),
                    url,
                    Random.nextInt(1, 999)
                )
            }
        }

        return START_STICKY
    }

    private fun createNotification(urlText: String, id: Int) {

        val notifyManager = NotifyManager(applicationContext)
        startForeground(id, notifyManager.setUpNotification(urlText).build())
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG, "Download Stopped")
    }


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}