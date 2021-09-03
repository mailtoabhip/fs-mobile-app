package com.delhivery.axle.ui.home.fragments.profile

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.response.MonthlyEarning
import com.delhivery.axle.config.UrlConfig.DashboardUrl
import com.delhivery.axle.databinding.FragmentHomeProfileBinding
import com.delhivery.axle.ui.home.activity.home.TitleProvider
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.ledger.consolidatedPageIntent
import com.delhivery.axle.ui.selectroute.SelectRouteFlowType.EditRoute
import com.delhivery.axle.ui.selectroute.activity.selectRouteIntent
import com.delhivery.axle.ui.team.teamMembersIntent
import com.delhivery.axle.ui.userroutes.userRoutesIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.prefs.UserPrefs
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
  @Inject lateinit var userPrefs : UserPrefs


  override fun getViewModelClass() = HomeProfileViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_profile

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    binding.error = false
    binding.loading = false
    binding.executePendingBindings()

    viewModel.tripEarningLiveData.reobserve(this, Observer { t ->
      binding.loading = false
      if (t != null) {
        binding.error = false
        updateTripMeter(t)
      } else {
        binding.error = true
        binding.containerError.title = "Session Timed out"
        binding.containerError.subTitle =
          "Unfortunately, we couldn't fetch the data you are looking for. \n" +
              " Kindly refresh."
        binding.containerError.actionLabel = "REFRESH"
      }
      binding.executePendingBindings()
    })

//    viewModel.userRoleLiveData.observe(this, Observer {
//      if (it && viewModel.userPrefs.isParent) {
//        binding.containerYourTeam.visibility = View.VISIBLE
//      } else {
//        binding.containerYourTeam.visibility = View.GONE
//      }
//    })

    if (viewModel.userPrefs.isParent) {
      binding.containerYourTeam.visibility = View.VISIBLE
    } else {
      binding.containerYourTeam.visibility = View.GONE
    }

    binding.containerYourRoutes.setOnClickListener {
      context?.let {
        startActivity(userRoutesIntent(it))
      }
    }

    binding.containerYourTeam.setOnClickListener {
      context?.let {
        startActivity(teamMembersIntent(it))
      }
    }

    binding.containerLogout.setOnClickListener { it.post { confirmLogout() } }

    binding.containerTripMeter.setOnClickListener {
      viewModel.fetchTripMeter()
    }

    binding.containerError.btnAction.setOnClickListener {
      binding.loading = true
      binding.executePendingBindings()
      viewModel.fetchTripMeter()
    }

    binding.containerPaymentTerms.setOnClickListener {
      when (contactUtils.openURL("${DashboardUrl.url()}/#/paymentterms")) {
        false -> uiUtils.showSnackbar("Could not open url")
      }
    }

    //viewModel.verifyRole()
    binding.containerYourMoney.setOnClickListener{
      context?.let {
        startActivity(consolidatedPageIntent(it))
      }
    }

    viewModel.fetchTripMeter()
  }

  private fun updateTripMeter(t: Map<Int, MonthlyEarning?>?) {
    binding.name = viewModel.userPrefs.userName
    binding.company = viewModel.userPrefs.companyName
    binding.mobile = viewModel.userPrefs.phoneNumber
    binding.bankAcc = viewModel.userPrefs.accNumber
    binding.ifsc = viewModel.userPrefs.ifscCode
    binding.pan = viewModel.userPrefs.pancard
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
    binding.executePendingBindings()
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
          analyticsUtil.trackEvent(
                  EVENT_USER_LOGOUT,
                  mutableListOf(PROPERTY_USER_ID , PROPERTY_TIME_SINCE_LAST_LOGIN),
                  mutableListOf(userPrefs.userId() , DateUtils.timeDiff(userPrefs.lastLoginTime))
          )
          viewModel.logout()
          navigationUtils.logout("Successfully logged out","fromUser")
        }
    )
  }
}