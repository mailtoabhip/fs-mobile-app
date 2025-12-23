package com.delhivery.axle.ui.tripdetails

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.config.UrlConfig
import com.delhivery.axle.ui.base.BaseViewModel
import javax.inject.Inject

/**
 * View model for [ImageViewActivity]
 */
class ImageViewModel @Inject constructor(
) : BaseViewModel() {

  val glideLiveData = MutableLiveData<String>()

  lateinit var url: String
  lateinit var type: String

  /**
   * @return url for image view
   */
  fun fetchImage() {
    if (url.startsWith("http", true) || url.startsWith("/") || url.startsWith("file://")) {
      glideLiveData.postValue(url)
    } else {
      glideLiveData.postValue(
          UrlConfig.ImageService.url() + url
      )
    }
  }

}