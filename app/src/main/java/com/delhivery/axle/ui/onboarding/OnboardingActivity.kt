package com.delhivery.axle.ui.onboarding

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.delhivery.axle.R
import com.delhivery.axle.config.OnboardingConfig
import com.delhivery.axle.databinding.ActivityOnboardingBinding
import com.delhivery.axle.databinding.ViewOnboardingBinding
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_ID
import com.delhivery.axle.ui.auth.AuthenticationActivity
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.custom.AnimationType.RevealOpen
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.utils.EVENT_SKIP_TUTORIAL
import com.delhivery.axle.utils.EVENT_VIEW_TUTORIAL
import com.github.florent37.kotlin.pleaseanimate.please

/**
 * First time user onboarding screen
 */
class OnboardingActivity : BaseActivity<ActivityOnboardingBinding, OnboardingViewModel>() {
  init {
    StatusBarColor = Color.parseColor("#181818")
  }

  override fun getViewModelClass() = OnboardingViewModel::class.java

  override fun layoutId() = R.layout.activity_onboarding

  override fun requireConnection() = false

  /* onboarding view pager adapter */
  private val adapter by lazy {
    OnboardingPagerAdapter()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    notificationId = intent?.extras?.getString(ARGS_NOTIFICATION_ID) ?: ""
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup view pager */
    binding.viewpager.apply {
      this.adapter = this@OnboardingActivity.adapter
      binding.pagerIndicator.viewPager = this
    }


    /* skip */
    binding.btnGetStarted.setOnClickListener { skip(true) }

    /* arc view animate reveal */
  /* make views visible */
      please {
        animate(binding.viewpager) toBe {
          visible()
        }
      }.start()

    if (notificationId.isNotEmpty()) {
      markNotificationRead()
    }
  }

  override fun markNotificationRead() {
    super.markNotificationRead()
    viewModel.markNotificationRead(notificationId)
  }

  /**
   * Move to next page
   */
  private fun moveNext() {
    analyticsUtil.trackEvent(EVENT_VIEW_TUTORIAL)
    val currentPage = binding.viewpager.currentItem
    if (currentPage < adapter.count - 1) {
      binding.viewpager.setCurrentItem(currentPage + 1, true)
    } else {
      skip()
    }
  }

  private fun skip(intentSkip :Boolean =false) {
    if(intentSkip) {
      analyticsUtil.trackEvent(EVENT_SKIP_TUTORIAL)
    }
    viewModel.onboardingCompleted()
    when (viewModel.isUserAuthenticated()) {
      true -> HomeActivity::class
      false -> AuthenticationActivity::class
    }.let { navigationUtils.navigate(it.java, true) }
  }

  /**
   * Onboarding pager adapter
   */
  inner class OnboardingPagerAdapter : PagerAdapter() {
    override fun isViewFromObject(
      p0: View,
      p1: Any
    ) = p0 == p1

    override fun getCount() = OnboardingConfig.size

    override fun instantiateItem(
      container: ViewGroup,
      position: Int
    ) = ViewOnboardingBinding.inflate(layoutInflater, container, true).apply {
      /* setup binding */
      val item = OnboardingConfig[position]
      title = item.title
      pageimage = item.image
      pageImage.setImageResource(item.image);
    }.root

    override fun destroyItem(
      container: ViewGroup,
      position: Int,
      obj: Any
    ) = if (obj is View) {
      container.removeView(obj)
    } else {
      super.destroyItem(container, position, obj)
    }
  }
}