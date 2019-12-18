package com.delhivery.axle.ui.base

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.delhivery.axle.BR
import com.delhivery.axle.R.string
import com.delhivery.axle.ui.home.activity.home.TitleProvider
import com.delhivery.axle.utils.AnalyticsUtil
import com.delhivery.axle.utils.Config.AxleSupportEmail
import com.delhivery.axle.utils.ContactUtils
import com.delhivery.axle.utils.ErrorUtils
import com.delhivery.axle.utils.UiUtils
import com.delhivery.axle.utils.extensions.disposeAndClear
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.DaggerFragment
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
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
  @Inject lateinit var contactUtils: ContactUtils

  /* set true if inline progress */
  protected var hasInlineProgress = false

  /* Make sure to add all disposables to compositeDisposables to avoid memory leaks and crashes */
  protected val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }

  private lateinit var permissionResultSubject: PublishSubject<Boolean>

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
        if (it == true) uiUtils.showProgress()
        else uiUtils.hideProgress()
      }
    })

    /* handle exception */
    viewModel.exceptionLiveData.observe(this, Observer {
      it?.let { throwable -> errorUtils.handle(throwable) }
    })
  }

  /**
   * remove observer and observe live data
   */
  fun <T> LiveData<T>.reobserve(
    owner: LifecycleOwner,
    observer: Observer<T>
  ) {
    removeObservers(owner)
    observe(owner, observer)
  }

  /**
   * Call helpline
   */
  fun callHelpline() {
    compositeDisposable += requestPermission(Manifest.permission.CALL_PHONE)
        .onBackground()
        .subscribe { granted, error ->
          if (error == null && granted) {
            when (contactUtils.callHelpline()) {
              false -> {
                uiUtils.showSnackbar("Unable to place call")
              }
              else -> {
              }
            }
          } else {
            uiUtils.showSnackbar(getString(string.msg_call_permission))
          }
        }
  }

  /**
   * Send emal
   */
  fun sendMail(email: String = AxleSupportEmail) {
    when (contactUtils.openGmail(receiver = email)) {
      false -> {
        uiUtils.showSnackbar("Sorry...You don't have any mail app installed")
      }
      else -> {
      }
    }
  }

  /**
   * Request Permission as Single<Boolean>
   *
   * @param permission [Manifest.permission] as String
   * @return Single<Boolean> with result
   */
  fun requestPermission(permission: String): Single<Boolean> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      return Single.just(true)
    }

    permissionResultSubject = PublishSubject.create()

    activity?.let {
      val permissionCode = ContextCompat.checkSelfPermission(it, permission)
      when (permissionCode) {
        PackageManager.PERMISSION_GRANTED -> {
          return Single.just(true)
        }
        else -> {
          ActivityCompat.requestPermissions(it, arrayOf(permission), PERMISSION_REQ_CODE)
        }
      }
    }
    return permissionResultSubject.singleOrError()
  }

  override fun onDestroy() {
    super.onDestroy()
    //dispose and clear all process
    compositeDisposable.disposeAndClear()
  }

  abstract fun getViewModelClass(): Class<VM>

  @LayoutRes protected abstract fun layoutId(): Int
}

/* permission req code */
private const val PERMISSION_REQ_CODE = 9020