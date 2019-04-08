package com.delhivery.orion.ui.onboarding

import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.delhivery.orion.R
import com.delhivery.orion.config.OnboardingConfig
import com.delhivery.orion.databinding.ActivityOnboardingBinding
import com.delhivery.orion.databinding.ViewOnboardingBinding
import com.delhivery.orion.ui.auth.AuthenticationActivity
import com.delhivery.orion.ui.base.BaseActivity
import com.delhivery.orion.ui.custom.AnimationType.RevealOpen
import com.delhivery.orion.ui.home.HomeActivity
import com.github.florent37.kotlin.pleaseanimate.please

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

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup view pager */
    binding.viewpager.apply {
      this.adapter = this@OnboardingActivity.adapter
      binding.pagerIndicator.viewPager = this
    }

    /* skip */
    binding.textSkip.setOnClickListener { skip() }

    /* next page fab */
    binding.fabNext.setOnClickListener { moveNext() }

    /* arc view animate reveal */
    binding.arcView.animate(RevealOpen) {
      /* make views visible */
      please {
        animate(binding.viewpager) toBe {
          visible()
        }
        animate(binding.containerActions) toBe {
          visible()
        }
      }.start()
    }
  }

  /**
   * Move to next page
   */
  private fun moveNext() {
    val currentPage = binding.viewpager.currentItem
    if (currentPage < adapter.count) {
      binding.viewpager.setCurrentItem(currentPage + 1, true)
    } else {
      skip()
    }
  }

  private fun skip() {
    viewModel.onboardingCompleted()
    when (viewModel.isUserAuthenticated()) {
      true -> HomeActivity::class
      false -> AuthenticationActivity::class
    }.let { navigationUtils.navigate(it.java, finishAfter = true) }
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
      message = item.message
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