package com.delhivery.axle.ui.tripdetails

import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class ImageViewModel @Inject constructor(
  private val userPrefs: UserPrefs
) : BaseViewModel() {

  val glideLiveData = MutableLiveData<GlideUrl>()

  lateinit var url: String
  lateinit var type: String

  fun fetchImage() {
    val glideUrl = GlideUrl(
        url,
        LazyHeaders.Builder()
            .addHeader("Authorization", "Bearer $userPrefs.jwtToken")
            .build()
    )

    glideLiveData.postValue(glideUrl)
  }

}