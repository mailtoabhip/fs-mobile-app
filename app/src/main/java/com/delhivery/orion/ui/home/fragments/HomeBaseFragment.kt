package com.delhivery.orion.ui.home.fragments

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.databinding.ViewDataBinding
import com.delhivery.orion.R
import com.delhivery.orion.ui.base.BaseFragment
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.ui.home.HomeActivity

abstract class HomeBaseFragment<B : ViewDataBinding, VM : BaseViewModel> : BaseFragment<B, VM>() {

  /* toolbar elevation Live Data */
  var toolbarElevationLiveData: MutableLiveData<Float>? = null

  /* elevation default value */
  protected val defToolbarElevation: Float  by lazy {
    resources.getDimension(R.dimen.toolbar_elevation)
  }

  fun <T> LiveData<T>.observeOnce(
    lifecycleOwner: LifecycleOwner,
    observer: Observer<T>
  ) {
    observe(lifecycleOwner, object : Observer<T> {
      override fun onChanged(t: T?) {
        observer.onChanged(t)
        removeObserver(this)
      }
    })
  }

  /**
   *
   * Post new action to activity
   *
   */
  protected fun action(action: BaseHomeFragmentAction) {
    (activity as HomeActivity?)?.apply { fragmentAction(action) }
  }
}