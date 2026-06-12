package com.dfd.delfin.ui.selectroute.fragments

import androidx.databinding.ViewDataBinding
import com.dfd.delfin.ui.base.BaseFragment
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.ui.selectroute.activity.SelectRouteActivity

abstract class SelectRouteBaseFragment<B : ViewDataBinding, VM : BaseViewModel> : BaseFragment<B, VM>() {

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