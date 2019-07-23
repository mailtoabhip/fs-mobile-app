package com.delhivery.axle.ui.tripdetails

import androidx.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityImageViewBinding
import com.delhivery.axle.injection.module.GlideApp
import com.delhivery.axle.ui.base.BaseActivity

/**
 * Created by saurabh
 * for Delhivery Private Limited
 **
 *
 *
 *
 **
 */
class ImageViewActivity : BaseActivity<ActivityImageViewBinding, ImageViewModel>() {

  override fun getViewModelClass() = ImageViewModel::class.java

  override fun layoutId() = R.layout.activity_image_view

  override fun requireConnection() = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    /* validate intent */
    if (intent == null || !intent.hasExtra(ImageUrlIntentKey)) {
      throw IllegalArgumentException("Required data $ImageUrlIntentKey not found")
    }

    /* set transaction id */
    viewModel.url = intent.getStringExtra(ImageUrlIntentKey)
    viewModel.type = intent.getStringExtra(ImageTypeIntentKey)
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    title = viewModel.type

    viewModel.glideLiveData.observe(this, Observer {
      GlideApp.with(this)
          .load(it)
          .into(binding.image)
    })

    viewModel.fetchImage()
  }
}

/* intent keys */
private const val ImageUrlIntentKey = "image_url"
private const val ImageTypeIntentKey = "image_type"

/**
 * Image view intent
 */
fun imageViewIntent(
  context: Context,
  _url: String,
  _type: String
) = Intent(context, ImageViewActivity::class.java).apply {
  putExtra(ImageUrlIntentKey, _url)
  putExtra(ImageTypeIntentKey, _type)
}
