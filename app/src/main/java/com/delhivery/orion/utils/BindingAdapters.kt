package com.delhivery.orion.utils

import android.animation.ObjectAnimator
import android.databinding.BindingAdapter
import android.graphics.Typeface
import android.support.v4.view.ViewCompat
import android.text.Html
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView

object BindingAdapters {

  @JvmStatic
  @BindingAdapter("shadow")
  fun setShadow(
    view: View,
    elevation: Float
  ) {
    ViewCompat.setElevation(view, elevation)
  }

  @JvmStatic
  @BindingAdapter("visibility")
  fun setVisibility(
    view: View,
    visible: Boolean
  ) {
    view.visibility = if (visible) View.VISIBLE else View.GONE
  }

  @JvmStatic
  @BindingAdapter(value = ["animateTo", "durationMs"])
  fun animateProgress(
    progressView: ProgressBar,
    progress: Int,
    duration: Int
  ) {
    val currentProgress = progressView.progress
    ObjectAnimator.ofInt(progressView, "progress", currentProgress, progress)
        .let {
          it.duration = duration.toLong()
          it.interpolator = DecelerateInterpolator()
          it
        }
        .start()
  }

  @JvmStatic
  @BindingAdapter("htmlText")
  fun setHtmlText(
    textView: TextView,
    html: String
  ) {
    textView.text = Html.fromHtml(html)
  }

  @JvmStatic
  @BindingAdapter("boldText")
  fun setBoldText(
    textView: TextView,
    isBold: Boolean
  ) {
    when (isBold) {
      true -> Typeface.BOLD
      false -> Typeface.NORMAL
    }.let {
      textView.setTypeface(null, it)
    }
  }

//    @JvmStatic
//    @BindingAdapter("bind:font")
//    fun bindFont(textView: TextView, fontName: String) {
//        textView.typeface = FontCache.getInstance(textView.context).get(fontName)
//    }
}
