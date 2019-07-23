package com.delhivery.axle.ui.home.fragments

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.R
import com.delhivery.axle.ui.base.BaseFragment
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.home.HomeActivity

abstract class HomeBaseFragment<B : ViewDataBinding, VM : BaseViewModel> : BaseFragment<B, VM>() {

  /* toolbar elevation Live Data */
  var toolbarElevationLiveData: MutableLiveData<Float>? = null

  var isLoadingData = true

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