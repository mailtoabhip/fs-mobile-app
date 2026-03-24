package com.delhivery.axle.utils

import android.animation.ObjectAnimator
import androidx.databinding.BindingAdapter
import android.graphics.Typeface
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import android.text.Html
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import com.delhivery.axle.R
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.utils.extensions.hasResource

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
    textView.text = HtmlCompat.fromHtml(html,FROM_HTML_MODE_LEGACY)
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

  @JvmStatic
  @BindingAdapter("app:srcCompat")
  fun bindSrcCompat(
    imageView: ImageView,
    @DrawableRes resId: Int
  ) {
    if (imageView.context.hasResource(resId)) {
      ContextCompat.getDrawable(imageView.context, resId)
          ?.let { drawable ->
            imageView.setImageDrawable(drawable)
          }
    }
  }

  @JvmStatic
  @BindingAdapter("bind:drawableResId")
  fun bindDrawableResId(
    view: View,
    @DrawableRes resId: Int
  ) {
    view.setBackgroundResource(resId)
  }

  @JvmStatic
  @BindingAdapter("optionalTextColor")
  fun setOptionalTextColor(view: TextView, request: HomeBidsRequestItemData?) {
    if (request == null) return

    val colorResId = if (request.isUnderTwoHourLoadAndContract()) {
        Log.d("setOptionalTextColor===>>>", "${request.isUnderTwoHourLoadAndContract()}")
      R.color.colorAccent
    } else {
      R.color.text_grey_v3
    }

    view.setTextColor(ContextCompat.getColor(view.context, colorResId))
  }

  @JvmStatic
  @BindingAdapter("deadlineTextColor")
  fun setDeadlineTextColor(view: TextView, colorResId: Int) {
    if (colorResId != 0) {
      view.setTextColor(ContextCompat.getColor(view.context, colorResId))
    }
  }


  @JvmStatic
  @BindingAdapter("strikethrough")
  fun setStrikethrough(textView: TextView, strikethrough: Boolean) {
    if (strikethrough) {
      textView.paintFlags = textView.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
    } else {
      textView.paintFlags = textView.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }
  }

  @JvmStatic
  @BindingAdapter("textColorRes")
  fun setTextColorRes(textView: TextView, colorResId: Int?) {
    if (colorResId != null && colorResId != 0) {
      textView.setTextColor(ContextCompat.getColor(textView.context, colorResId))
    }
  }

//    @JvmStatic
//    @BindingAdapter("bind:font")
//    fun bindFont(textView: TextView, fontName: String) {
//        textView.typeface = FontCache.getInstance(textView.context).get(fontName)
//    }
}
