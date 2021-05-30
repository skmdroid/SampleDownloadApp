package com.skmdroid.sampledownloadapp

import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver

class DownloadResultReceiver(handler: Handler) : ResultReceiver(handler) {
    private var mReceiver: Receiver? = null

    fun setReceiver(value: Receiver) {
        mReceiver = value
    }

    interface Receiver {
        fun onReceiveResult(resultCode: Int, resultData: Bundle?)
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        mReceiver?.onReceiveResult(resultCode, resultData)
    }
}