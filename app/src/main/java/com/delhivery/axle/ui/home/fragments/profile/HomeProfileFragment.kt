package com.delhivery.axle.ui.home.fragments.profile

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentHomeProfileBinding
import com.delhivery.axle.ui.home.TitleProvider
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.selectroute.SelectRouteFlowType.EditRoute
import com.delhivery.axle.ui.selectroute.activity.selectRouteIntent
import com.delhivery.axle.utils.DialogUtils
import com.delhivery.axle.utils.EVENT_EDIT_ROUTE
import com.delhivery.axle.utils.NavigationUtils
import com.delhivery.axle.utils.PROPERTY_SOURCE
import com.delhivery.axle.utils.VALUE_PROFILE
import javax.inject.Inject

class HomeProfileFragment : HomeBaseFragment<FragmentHomeProfileBinding, HomeProfileViewModel>(),
    TitleProvider {

  override val title: CharSequence
    get() = "Profile"

  init {
    hasInlineProgress = true
  }

  companion object {
    /* singleton instance */
    val _instance: HomeProfileFragment by lazy { HomeProfileFragment() }
  }

  @Inject lateinit var dialogUtils: DialogUtils

  override fun getViewModelClass() = HomeProfileViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_profile

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    binding.apply {
      containerYourRoutes.setOnClickListener {
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_EDIT_ROUTE,
            mutableListOf(PROPERTY_SOURCE),
            mutableListOf(VALUE_PROFILE)
        )
        it.post {
          startActivity(
              selectRouteIntent(it.context, EditRoute)
          )
        }
      }

      containerLogout.setOnClickListener { it.post { confirmLogout() } }
    }

    binding.containerTripMeter.setOnClickListener {
      viewModel.fetchTripMeter()
    }

    viewModel.tripEarningLiveData.observe(this, Observer { t ->
      if (t != null && t.size == 2) {
        val keys = t.keys.toMutableList()
        val key1 = keys[0]
        val key2 = keys[1]
        if (key1 == 1 && key2 == 12) {
          binding.lastMonth = t[key2]
          binding.currentMonth = t[key1]
        } else {
          binding.lastMonth = t[key1]
          binding.currentMonth = t[key2]
        }
      }
    })

    viewModel.fetchTripMeter()
  }

  /**
   * Confirm and logout
   */
  private fun confirmLogout() {
    dialogUtils.showBasicConfirmDialog(
        R.string.title_dialog_logout,
        R.string.msg_dialog_logout,
        positiveAction = "LOGOUT",
        negativeAction = "BACK",
        positiveClickListener = {
          it.dismiss()
          viewModel.logout()
          navigationUtils.logout("Successfully logged out")
        }
    )
  }
}