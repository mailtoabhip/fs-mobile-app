package com.dfd.delfin.ui.searchload.fragments

import androidx.databinding.ViewDataBinding
import com.dfd.delfin.ui.base.BaseFragment
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.ui.searchload.SearchLoadActivity

/**
 * Base fragment for Search load and result
 */
abstract class SearchLoadBaseFragment<B : ViewDataBinding, VM : BaseViewModel> : BaseFragment<B, VM>() {

  var isLoadingData=true
  /**
   * Post new action to activity
   */
  protected fun action(action: BaseSearchLoadFragmentAction) {
    (activity as SearchLoadActivity?)?.apply { fragmentAction(action) }
  }
}