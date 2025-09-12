package com.delhivery.axle.ui.profile.raterewards.fragments

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.delhivery.axle.ui.base.BaseFragment
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.profile.raterewards.ShareRateGetRewardsActivity

abstract class ShareRateGetRewardsBaseFragment<B : ViewDataBinding, VM : BaseViewModel> : BaseFragment<B, VM>() {


  init {
    hasInlineProgress = true
  }

  /**
   * refresh screen data
   */
  abstract fun refreshData()

  fun <T> LiveData<T>.observeOnce(
    lifecycleOwner: LifecycleOwner,
    observer: Observer<T>
  ) {
    observe(lifecycleOwner, object : Observer<T> {
      override fun onChanged(t: T) {
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
  protected fun action(action: BaseShareRateGetRewardsFragmentAction) {
    (activity as ShareRateGetRewardsActivity?)?.apply { fragmentAction(action) }
  }
}