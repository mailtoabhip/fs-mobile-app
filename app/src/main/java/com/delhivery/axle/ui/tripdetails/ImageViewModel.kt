package com.delhivery.axle.ui.tripdetails

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class ImageViewModel @Inject constructor(
  private val userPrefs: UserPrefs
) : BaseViewModel() {

  val glideLiveData = MutableLiveData<String>()

  lateinit var url: String
  lateinit var type: String

  fun fetchImage() {
//    val glideUrl = GlideUrl(
//        url,
//        LazyHeaders.Builder()
//            .addHeader("Authorization", "Bearer $userPrefs.jwtToken")
//            .build()
//    )

    glideLiveData.postValue("https://e4l81arqid.execute-api.ap-southeast-1.amazonaws.com/p4/" + url)
  }

}