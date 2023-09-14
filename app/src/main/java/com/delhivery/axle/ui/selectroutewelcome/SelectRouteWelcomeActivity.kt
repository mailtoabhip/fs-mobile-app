package com.delhivery.axle.ui.selectroutewelcome

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivitySelectRouteWelcomeBinding
import com.delhivery.axle.ui.base.BaseLocationActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.selectroute.activity.SelectRouteWelcomeIntentExtra
import com.delhivery.axle.ui.selectroute.activity.selectRouteIntent
import com.delhivery.axle.utils.LocationFlowState
import com.delhivery.axle.utils.REQCODE_ADD_ROUTES
import com.github.florent37.kotlin.pleaseanimate.please

class SelectRouteWelcomeActivity : BaseLocationActivity<ActivitySelectRouteWelcomeBinding, SelectRouteWelcomeViewModel>() {

  init {
    StatusBarColor = Color.parseColor("#181818")
  }

  override fun getViewModelClass() = SelectRouteWelcomeViewModel::class.java

  override fun layoutId() = R.layout.activity_select_route_welcome

  override fun requireConnection() = false

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

  private fun animate() {
    please {
      animate(binding.imageHeader) toBe {
        visible()
        aboveOf(binding.headerBaselineView)
      }
    }.thenCouldYou {
      animate(binding.textWelcomeUser) toBe {
        visible()
      }
      animate(binding.textGladToOnboard) toBe {
        visible()
      }
      animate(binding.containerActions) toBe {
        visible()
      }
    }
        .start()
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