package com.delhivery.axle.ui.accountsetup

import androidx.databinding.ViewDataBinding
import com.delhivery.axle.ui.base.BaseFragment
import com.delhivery.axle.ui.base.BaseViewModel
/**
 * Base fragment for Account set up
 */
abstract class AccountSetupBaseFragment<B : ViewDataBinding, VM : BaseViewModel> : BaseFragment<B, VM>() {

  /**
   * Post new action to activity
   */
  protected fun action(action: BaseAccountSetupFragmentAction) {
    (activity as AccountSetupActivity?)?.apply { fragmentAction(action) }
  }
}