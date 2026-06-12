package com.dfd.delfin.ui.profile

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.dfd.delfin.R
import com.dfd.delfin.api.response.MonthlyEarning
import com.dfd.delfin.config.UrlConfig.DashboardUrl
import com.dfd.delfin.databinding.FragmentHomeProfileBinding
import com.dfd.delfin.ui.home.activity.home.TitleProvider
import com.dfd.delfin.ui.home.fragments.HomeBaseFragment
import com.dfd.delfin.ui.ledger.consolidatedPageIntent
import com.dfd.delfin.ui.team.teamMembersIntent
import com.dfd.delfin.ui.userroutes.userRoutesIntent
import com.dfd.delfin.utils.*
import com.dfd.delfin.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
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

  private var fragmentSetupTrace: Trace? = null
  private var isFirstResume = true

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    fragmentSetupTrace = FirebasePerformance.getInstance().newTrace("HomeProfileFragment_SetupTime")
    fragmentSetupTrace?.start()
    binding.error = false
    binding.loading = false
    binding.executePendingBindings()

    viewModel.logoutResultLiveData.observe(this, Observer { success ->
      if (success == true) {
        navigationUtils.logout("Successfully logged out", "fromUser")
      } else {
        uiUtils.showSnackbar("Logout failed. Please try again.")
      }
    })

    viewModel.tripEarningLiveData.reobserve(this, Observer { t ->
      binding.loading = false
      if (t != null) {
        binding.error = false
        updateTripMeter(t)
      } else {
        binding.name = viewModel.userPrefs.userName
        binding.company = viewModel.userPrefs.companyName
        binding.mobile = viewModel.userPrefs.phoneNumber
        binding.bankAcc = viewModel.userPrefs.accNumber
        binding.ifsc = viewModel.userPrefs.ifscCode
        binding.pan = viewModel.userPrefs.pancard
//        binding.error = true
//        binding.containerError.title = "Session Timed out"
//        binding.containerError.subTitle =
//          "Unfortunately, we couldn't fetch the data you are looking for. \n" +
//              " Kindly refresh."
//        binding.containerError.actionLabel = "REFRESH"
//      }
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
        userPrefs.setPreviousScreen(this.javaClass.name)
        startActivity(userRoutesIntent(it))
      }
    }

    binding.containerYourTeam.setOnClickListener {
      context?.let {
        userPrefs.setPreviousScreen(this.javaClass.name)
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
        else ->{}
      }
    }

    //viewModel.verifyRole()
    binding.containerYourMoney.setOnClickListener{
      context?.let {
        userPrefs.setPreviousScreen(this.javaClass.name)
        startActivity(consolidatedPageIntent(it))
      }
    }

    viewModel.fetchTripMeter()
  }

  override fun onResume() {
    super.onResume()
    if (fragmentSetupTrace != null && isFirstResume) {
      fragmentSetupTrace?.stop()
      isFirstResume = false
    }
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
          analyticsUtil.moEngageTrackEvent(
                  EVENT_USER_LOGOUT,
                  mutableListOf(PROPERTY_USER_ID , PROPERTY_TIME_SINCE_LAST_LOGIN),
                  mutableListOf(userPrefs.userId() , DateUtils.timeDiff(userPrefs.lastLoginTime))
          )
          viewModel.logout()
        }
    )
  }
}