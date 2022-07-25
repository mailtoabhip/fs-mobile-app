package com.delhivery.axle.ui.profile.raterewards


import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityShareRateGetRewardsBinding
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_ID
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_TYPE
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.home.activity.home.OFFER_APPROVED
import com.delhivery.axle.ui.home.fragments.HomeFragmentType.LoadsTruckFragment
import com.delhivery.axle.ui.home.fragments.NavigateHomeFragmentAction
import com.delhivery.axle.ui.profile.raterewards.fragments.BaseShareRateGetRewardsFragmentAction
import com.delhivery.axle.ui.profile.raterewards.fragments.NavigateShareRateGetRewardsFragmentAction
import com.delhivery.axle.ui.profile.raterewards.fragments.ShareRateGetRewardsFragmentActionType.Navigate
import com.delhivery.axle.ui.profile.raterewards.fragments.ShareRateGetRewardsFragmentAdapter
import com.delhivery.axle.ui.profile.raterewards.fragments.ShareRateGetRewardsFragmentType
import com.delhivery.axle.ui.profile.raterewards.fragments.ShareRateGetRewardsFragmentType.RewardsFragment
import com.delhivery.axle.utils.prefs.UserPrefs
import kotlinx.android.synthetic.main.view_your_rewards_item.offer_text
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

    if((intent?.extras?.getString(ARGS_NOTIFICATION_TYPE)
        ?: "") == OFFER_APPROVED
    ){
    fragmentAction((NavigateShareRateGetRewardsFragmentAction(RewardsFragment)))
    }
  }

  /**
   * Fragment action observer
   */
  fun fragmentAction(action: BaseShareRateGetRewardsFragmentAction) {
    when (action.type) {
      /* navigate to fragment action */
      Navigate -> {
        val fragmentType = (action as NavigateShareRateGetRewardsFragmentAction).fragmentType
        binding.viewpager.setCurrentItem(fragmentType.position, true)
      }
    }
  }
}
