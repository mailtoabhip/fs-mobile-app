package com.delhivery.axle.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.delhivery.axle.BR
import com.delhivery.axle.ui.home.activity.home.TitleProvider
import com.delhivery.axle.utils.AnalyticsUtil
import com.delhivery.axle.utils.ErrorUtils
import com.delhivery.axle.utils.UiUtils
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * Created by saurabh on 26/03/18.
 */

abstract class BaseFragment<B : ViewDataBinding, VM : BaseViewModel> : DaggerFragment(),
    LifecycleOwner, TitleProvider {

  override val title: CharSequence
    get() = ""

  protected lateinit var binding: B
  lateinit var viewModel: VM

  @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
  @Inject lateinit var uiUtils: UiUtils
  @Inject lateinit var errorUtils: ErrorUtils
  @Inject lateinit var analyticsUtil: AnalyticsUtil

  /* set true if inline progress */
  protected var hasInlineProgress = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    AndroidSupportInjection.inject(this)

    viewModel = ViewModelProviders.of(this, viewModelFactory)
        .get(getViewModelClass())
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = DataBindingUtil.inflate(inflater, layoutId(), container, false)
    binding.setVariable(BR.viewModel, viewModel)
    return binding.root
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    /* Observe on toast live data and show toast */
    viewModel.toastLiveData.observe(this, Observer {
      it?.let { uiUtils.showToast(it) }
    })

    /* Observe on snackbar live data and show snackbar */
    viewModel.snackbarLiveData.observe(this, Observer {
      it?.let { uiUtils.showSnackbar(it) }
    })

    /* Observe on progress live data and show/hide progress */
    viewModel.progressLiveData.observe(this, Observer {
      if (!hasInlineProgress) {
        when (it) {
          true -> uiUtils.showProgress()
          else -> uiUtils.hideProgress()
        }
      }
    })

    /* handle exception */
    viewModel.exceptionLiveData.observe(this, Observer {
      it?.let { throwable -> errorUtils.handle(throwable) }
    })
  }

  fun <T> LiveData<T>.reobserve(
    owner: LifecycleOwner,
    observer: Observer<T>
  ) {
    removeObservers(owner)
    observe(owner, observer)
  }

  abstract fun getViewModelClass(): Class<VM>

  @LayoutRes protected abstract fun layoutId(): Int
}