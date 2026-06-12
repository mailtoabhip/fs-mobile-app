package com.dfd.delfin.ui.tripdetails

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.config.UrlConfig
import com.dfd.delfin.ui.base.BaseViewModel
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