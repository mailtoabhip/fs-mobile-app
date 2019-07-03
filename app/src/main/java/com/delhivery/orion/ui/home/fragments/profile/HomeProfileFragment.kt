package com.delhivery.orion.ui.home.fragments.profile

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.databinding.FragmentHomeProfileBinding
import com.delhivery.orion.ui.home.TitleProvider
import com.delhivery.orion.ui.home.fragments.HomeBaseFragment
import com.delhivery.orion.ui.selectroute.SelectRouteFlowType.EditRoute
import com.delhivery.orion.ui.selectroute.activity.selectRouteIntent
import com.delhivery.orion.utils.DialogUtils
import com.delhivery.orion.utils.NavigationUtils
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
  @Inject lateinit var navigationUtils: NavigationUtils

  override fun getViewModelClass() = HomeProfileViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_profile

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    binding.apply {
      containerYourRoutes.setOnClickListener {
        it.post {
          startActivity(
              selectRouteIntent(it.context, EditRoute)
          )
        }
      }

      containerLogout.setOnClickListener { it.post { confirmLogout() } }
    }

    viewModel.tripEarningLiveData.observe(this, Observer { t ->
      if (t != null && t.size == 2) {
        val keys = t.keys.toMutableList()
        val key1 = keys.get(0)
        val key2 = keys.get(1)
        if (key1 == 1 && key2 == 12) {
          binding.lastMonth = t.get(key2)
          binding.currentMonth = t.get(key1)
        } else {
          binding.lastMonth = t.get(key1)
          binding.currentMonth = t.get(key2)
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
        negativeAction = "BACK"
    ) {
      it.dismiss()
      navigationUtils.logout("Successfully logged out")
    }
  }
}