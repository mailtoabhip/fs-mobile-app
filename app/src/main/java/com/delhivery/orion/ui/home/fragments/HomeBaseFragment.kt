package com.delhivery.orion.ui.home.fragments

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.databinding.ViewDataBinding
import com.delhivery.orion.R
import com.delhivery.orion.ui.base.BaseFragment
import com.delhivery.orion.ui.home.HomeActivity

abstract class HomeBaseFragment<B : ViewDataBinding, VM : ViewModel> : BaseFragment<B, VM>() {

  /* toolbar elevation Live Data */
  var toolbarElevationLiveData: MutableLiveData<Float>? = null

  /* elevation default value */
  protected val defToolbarElevation: Float  by lazy {
    resources.getDimension(R.dimen.toolbar_elevation)
  }

  /**
   * Post new action to activity
   */
  protected fun action(action: BaseHomeFragmentAction) {
    (activity as HomeActivity?)?.apply { fragmentAction(action) }
  }
}