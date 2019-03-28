package com.delhivery.orion.ui.selectroute.fragments

import android.arch.lifecycle.ViewModel
import android.databinding.ViewDataBinding
import com.delhivery.orion.ui.base.BaseFragment
import com.delhivery.orion.ui.selectroute.SelectRouteActivity

abstract class SelectRouteBaseFragment<B : ViewDataBinding, VM : ViewModel> : BaseFragment<B, VM>() {

  /**
   * /**
   * Post new action to activity
  */
  protected fun action(action: BaseSearchLoadFragmentAction) {
  (activity as SearchLoadActivity?)?.apply { fragmentAction(action) }
  }
   */

  /**
   * Post new action to activity
   */
  protected fun action(action: BaseSelectRouteFragmentAction) {
    (activity as SelectRouteActivity?)?.apply { fragmentAction(action) }
  }
}