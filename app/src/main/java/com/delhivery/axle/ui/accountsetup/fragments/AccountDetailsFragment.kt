package com.delhivery.axle.ui.accountsetup.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentAccountDetailsBinding
import com.delhivery.axle.databinding.FragmentPrimaryActionBinding
import com.delhivery.axle.ui.accountsetup.AccountSetupActivity
import com.delhivery.axle.ui.accountsetup.AccountSetupBaseFragment
import com.delhivery.axle.ui.accountsetup.AccountSetupViewModel
import com.delhivery.axle.ui.kyc.gst.GstVerificationActivity
import com.delhivery.axle.ui.kyc.pan.PanVerificationActivity

/**
 * Account Details set up
 */
class AccountDetailsFragment : AccountSetupBaseFragment<FragmentAccountDetailsBinding, AccountSetupViewModel>() {

  companion object {
    /* singleton instance */
    val _instance: AccountDetailsFragment by lazy { AccountDetailsFragment() }
  }

  override fun getViewModelClass() = AccountSetupViewModel::class.java

  override fun layoutId() = R.layout.fragment_account_details

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.progressLiveData.observe(this, ProgressObserver())

    binding.btnCreateAccount.setOnClickListener {
      startActivity(context?.let { it1 -> panIntent(it1) })
    }
  }

  override fun onPause() {
    super.onPause()
    uiUtils.toggleKeyboard()
  }

  /**
   * Progress observer
   */
  inner class ProgressObserver : Observer<Boolean> {
    override fun onChanged(t: Boolean?) {
      t?.let {
        when (t) {
          true -> uiUtils.showDelhiveryProgress(
              "Getting details", "This usually takes few seconds to load. please be patient.",
              "This usually takes few seconds to load. please be patient."
          )
          false -> uiUtils.hideDelhiveryProgress()
        }
      }
    }
  }

  fun panIntent(
          context: Context
  ): Intent = Intent(context, PanVerificationActivity::class.java).apply {

  }

}