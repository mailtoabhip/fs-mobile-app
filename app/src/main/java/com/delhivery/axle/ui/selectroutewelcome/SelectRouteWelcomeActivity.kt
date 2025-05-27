package com.delhivery.axle.ui.selectroutewelcome

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.ContractType
import com.delhivery.axle.databinding.ActivitySelectRouteWelcomeBinding
import com.delhivery.axle.ui.base.BaseLocationActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.selectroute.activity.SelectRouteWelcomeIntentExtra
import com.delhivery.axle.ui.selectroute.activity.selectRouteIntent
import com.delhivery.axle.utils.LocationFlowState
import com.delhivery.axle.utils.REQCODE_ADD_ROUTES
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

class SelectRouteWelcomeActivity : BaseLocationActivity<ActivitySelectRouteWelcomeBinding, SelectRouteWelcomeViewModel>() {

  init {
    StatusBarColor = Color.parseColor("#181818")
  }

  override fun getViewModelClass() = SelectRouteWelcomeViewModel::class.java

  override fun layoutId() = R.layout.activity_select_route_welcome

  override fun requireConnection() = false

  private var activitySetupTrace: Trace? = null
  private var isFirstResume = true
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activitySetupTrace = FirebasePerformance.getInstance().newTrace("SelectRouteWelcomeActivity_SetupTime")
    activitySetupTrace?.start()
  }
  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    /* select route submit button */
    binding.btnSelectRoute.setOnClickListener {
      val bundle = Bundle()
      bundle.putBoolean(SelectRouteWelcomeIntentExtra, true)
      navigationUtils.navigateForActivityResult(
          intent = selectRouteIntent(this@SelectRouteWelcomeActivity),
          requestCode = REQCODE_ADD_ROUTES, extras = bundle
      )
    }

    /* skip button functionality */
    binding.textSkip.setOnClickListener {
      navigationUtils.navigate(HomeActivity::class.java, true)
    }

    /* animate and open bottom view */
    binding.arcView.animate { /*end action start animtion chain*/ animate() }
  }
  override fun onResume() {
    super.onResume()
    if (activitySetupTrace != null && isFirstResume) {
      activitySetupTrace?.stop()
      isFirstResume = false
    }
  }
  private fun animate() {
    val alphaAnimator1 = ObjectAnimator.ofFloat(binding.imageHeader, "alpha", 0f,   1f)
    val alphaAnimator2 = ObjectAnimator.ofFloat(binding.textWelcomeUser, "alpha", 0f, 1f)
    val alphaAnimator3 = ObjectAnimator.ofFloat(binding.textGladToOnboard, "alpha", 0f, 1f)
    val alphaAnimator4 = ObjectAnimator.ofFloat(binding.containerActions, "alpha", 0f, 1f)

    val animatorSet = AnimatorSet()
    animatorSet.playTogether(alphaAnimator1,alphaAnimator2,alphaAnimator3,alphaAnimator4)
    animatorSet.start()
  }

  override fun updateLocationFlowState(flowState: LocationFlowState) {
    /* not needed as of now */
  }

  val resultLauncher = registerForActivityResult(StartActivityForResult()){ result ->
    
  }
  /*override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      REQCODE_ADD_ROUTES -> navigationUtils.navigate(
          HomeActivity::class.java, true
      )
    }
  }*/

}