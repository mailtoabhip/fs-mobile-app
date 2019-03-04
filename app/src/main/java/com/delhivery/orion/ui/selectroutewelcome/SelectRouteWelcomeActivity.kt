package com.delhivery.orion.ui.selectroutewelcome

import android.graphics.Color
import android.os.Bundle
import com.delhivery.orion.R
import com.delhivery.orion.databinding.ActivitySelectRouteWelcomeBinding
import com.delhivery.orion.ui.base.BaseLocationActivity
import com.delhivery.orion.ui.selectroute.SelectRouteActivity
import com.delhivery.orion.utils.LocationFlowState
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
      if (onLocationButtonClicked()) {
        navigationUtils.navigate(SelectRouteActivity::class.java, finishAfter = true)
      }
    }

    /* skip button functionality - todo */

    /* animate and open bottom view */
    binding.arcView.animateOpen { /*end action start animtion chain*/ animate() }
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
}