package com.delhivery.axle.ui.accountsetup.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentAccountRoleBinding
import com.delhivery.axle.databinding.FragmentPrimaryActionBinding
import com.delhivery.axle.ui.accountsetup.AccountSetupBaseFragment
import com.delhivery.axle.ui.accountsetup.AccountSetupViewModel
import com.delhivery.axle.ui.searchload.fragments.SearchLoadBaseFragment

/**
 * Account Role action
 */
class AccountRoleFragment : AccountSetupBaseFragment<FragmentAccountRoleBinding, AccountSetupViewModel>() {

  companion object {
    /* singleton instance */
    val _instance: AccountRoleFragment by lazy { AccountRoleFragment() }
  }

  override fun getViewModelClass() = AccountSetupViewModel::class.java

  override fun layoutId() = R.layout.fragment_account_role

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.progressLiveData.observe(this, ProgressObserver())
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
}