package com.delhivery.axle.ui.profile.raterewards


import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityShareRateGetRewardsBinding
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_TYPE
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.profile.raterewards.fragments.BaseShareRateGetRewardsFragmentAction
import com.delhivery.axle.ui.profile.raterewards.fragments.NavigateShareRateGetRewardsFragmentAction
import com.delhivery.axle.ui.profile.raterewards.fragments.ShareRateGetRewardsFragmentActionType.Navigate
import com.delhivery.axle.ui.profile.raterewards.fragments.ShareRateGetRewardsFragmentAdapter
import com.delhivery.axle.ui.profile.raterewards.fragments.ShareRateGetRewardsFragmentType
import com.delhivery.axle.ui.profile.raterewards.fragments.ShareRateGetRewardsFragmentType.RewardsFragment
import com.delhivery.axle.utils.WindowInsetsUtils
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject


class ShareRateGetRewardsActivity: BaseActivity<ActivityShareRateGetRewardsBinding, ShareRateGetRewardsViewModel>() {

  override fun getViewModelClass() = ShareRateGetRewardsViewModel::class.java

  override fun layoutId() = R.layout.activity_share_rate_get_rewards

  override fun requireConnection() = true

  @Inject lateinit var userPrefs: UserPrefs

  /*  fragments pager adapter */
  private lateinit var pagerAdapter: ShareRateGetRewardsFragmentAdapter

  private var activitySetupTrace: Trace? = null
  private var isFirstResume = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activitySetupTrace = FirebasePerformance.getInstance().newTrace("ShareRateGetRewardsActivity_SetupTime")
    activitySetupTrace?.start()
  }
  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    setSupportActionBar(binding.toolbar)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    title = "Share & Earn reward"
    pagerAdapter = ShareRateGetRewardsFragmentAdapter(supportFragmentManager)

    binding.viewpager.apply {
      offscreenPageLimit = ShareRateGetRewardsFragmentType.count()
      adapter = pagerAdapter
    }

    binding.shareRateRewardsTabLayout.setupWithViewPager(binding.viewpager)
  }

  override fun onResume() {
    super.onResume()
    if (activitySetupTrace != null && isFirstResume) {
      activitySetupTrace?.stop()
      isFirstResume = false
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
