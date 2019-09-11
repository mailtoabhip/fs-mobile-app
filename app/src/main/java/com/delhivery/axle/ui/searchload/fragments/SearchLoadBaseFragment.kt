package com.delhivery.axle.ui.searchload.fragments

import androidx.databinding.ViewDataBinding
import com.delhivery.axle.ui.base.BaseFragment
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.searchload.SearchLoadActivity

abstract class SearchLoadBaseFragment<B : ViewDataBinding, VM : BaseViewModel> : BaseFragment<B, VM>() {

  /**
   * Post new action to activity
   */
  protected fun action(action: BaseSearchLoadFragmentAction) {
    (activity as SearchLoadActivity?)?.apply { fragmentAction(action) }
  }
}