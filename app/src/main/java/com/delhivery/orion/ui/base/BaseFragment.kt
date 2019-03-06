package com.delhivery.orion.ui.base

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.delhivery.orion.BR
import com.delhivery.orion.utils.UiUtils
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * Created by Harish on 26/03/18.
 */

abstract class BaseFragment<B : ViewDataBinding, VM : ViewModel> : DaggerFragment(),
    LifecycleOwner {
  protected lateinit var binding: B
  lateinit var viewModel: VM

  @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
  @Inject lateinit var uiUtils: UiUtils

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

  abstract fun getViewModelClass(): Class<VM>

  @LayoutRes protected abstract fun layoutId(): Int
}