package com.delhivery.orion.ui.searchload.fragments

import android.arch.lifecycle.ViewModel
import android.databinding.ViewDataBinding
import com.delhivery.orion.ui.base.BaseFragment
import com.delhivery.orion.ui.searchload.SearchLoadActivity

abstract class SearchLoadBaseFragment<B : ViewDataBinding, VM : ViewModel> : BaseFragment<B, VM>() {

  /**
   * Post new action to activity
   */
  protected fun action(action: BaseSearchLoadFragmentAction) {
    (activity as SearchLoadActivity?)?.apply { fragmentAction(action) }
  }
}