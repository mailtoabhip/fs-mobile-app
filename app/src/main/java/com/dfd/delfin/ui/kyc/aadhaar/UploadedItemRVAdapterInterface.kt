package com.dfd.delfin.ui.kyc.aadhaar


interface UploadedItemRVAdapterInterface {
    fun handleDeleteAction(item : Pair<String,String>)
}

interface DownloadtemRVAdapterInterface {
    fun handleImageAction(item : Pair<String,String?>)

    fun handleAction(item : String)
}

