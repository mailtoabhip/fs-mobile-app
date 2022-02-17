package com.delhivery.axle.ui.kyc.aadhaar

import android.widget.ImageView
import android.widget.TextView


interface UploadedItemRVAdapterInterface {
    fun handleDeleteAction(item : Pair<String,String>)
}

interface DownloadtemRVAdapterInterface {
    fun handleImageAction(item : Pair<String,String?>)

    fun handleAction(item : String)
}

