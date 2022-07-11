package com.delhivery.axle.ui.profile.raterewards


import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityShareRateGetRewardsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.profile.raterewards.fragments.ShareRateGetRewardsFragmentAdapter
import com.delhivery.axle.ui.profile.raterewards.fragments.ShareRateGetRewardsFragmentType
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject




class ShareRateGetRewardsActivity: BaseActivity<ActivityShareRateGetRewardsBinding, ShareRateGetRewardsViewModel>() {

  override fun getViewModelClass() = ShareRateGetRewardsViewModel::class.java

  override fun layoutId() = R.layout.activity_share_rate_get_rewards

  override fun requireConnection() = true

  @Inject lateinit var userPrefs: UserPrefs

  /*  fragments pager adapter */
  private lateinit var pagerAdapter: ShareRateGetRewardsFragmentAdapter

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    title = "Share & Earn reward"
    pagerAdapter = ShareRateGetRewardsFragmentAdapter(supportFragmentManager)

    binding.viewpager.apply {
      offscreenPageLimit = ShareRateGetRewardsFragmentType.count()
      adapter = pagerAdapter
    }

    binding.shareRateRewardsTabLayout.setupWithViewPager(binding.viewpager)

  }

}