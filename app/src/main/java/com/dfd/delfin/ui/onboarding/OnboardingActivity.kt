package com.dfd.delfin.ui.onboarding

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.dfd.delfin.R
import com.dfd.delfin.config.OnboardingConfig
import com.dfd.delfin.databinding.ActivityOnboardingBinding
import com.dfd.delfin.databinding.ViewOnboardingBinding
import com.dfd.delfin.fcm.ARGS_NOTIFICATION_ID
import com.dfd.delfin.ui.auth.AuthenticationActivity
import com.dfd.delfin.ui.base.BaseActivity
import com.dfd.delfin.ui.home.activity.home.HomeActivity
import com.dfd.delfin.utils.EVENT_SKIP_TUTORIAL
import com.dfd.delfin.utils.EVENT_VIEW_TUTORIAL


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
    val animator = ObjectAnimator.ofFloat(binding.viewpager, "alpha", 0f, 1f)
    animator.start()

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
    analyticsUtil.moEngageTrackEvent(EVENT_VIEW_TUTORIAL)
    val currentPage = binding.viewpager.currentItem
    if (currentPage < adapter.count - 1) {
      binding.viewpager.setCurrentItem(currentPage + 1, true)
    } else {
      skip()
    }
  }

  private fun skip(intentSkip :Boolean =false) {
    if(intentSkip) {
      analyticsUtil.moEngageTrackEvent(EVENT_SKIP_TUTORIAL)
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