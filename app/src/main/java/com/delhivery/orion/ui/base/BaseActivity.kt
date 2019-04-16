package com.delhivery.orion.ui.base

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatDelegate
import android.view.MenuItem
import com.delhivery.orion.BR
import com.delhivery.orion.network.ConnectionLiveData
import com.delhivery.orion.utils.ErrorUtils
import com.delhivery.orion.utils.NavigationUtils
import com.delhivery.orion.utils.UiUtils
import com.delhivery.orion.utils.extensions.disposeAndClear
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * Base Activity extends [DaggerAppCompatActivity]
 *
 * Binds layout id to binding class
 * Provide ViewModel
 *
 * @property B Generated Binding Class
 * @property VM ViewModelClass, should extend [BaseViewModel]
 *
 */
abstract class BaseActivity<B : ViewDataBinding, VM : BaseViewModel> : DaggerAppCompatActivity(),
    LifecycleObserver {
  init {
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
  }

  /* override in init if required for specific activity */
  protected var StatusBarColor = Color.parseColor("#F9F9F9")

  protected lateinit var binding: B
  protected lateinit var viewModel: VM

  @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
  @Inject lateinit var connectionLiveData: ConnectionLiveData
  @Inject lateinit var uiUtils: UiUtils
  @Inject lateinit var navigationUtils: NavigationUtils
  @Inject lateinit var errorUtils: ErrorUtils

  private lateinit var permissionResultSubject: PublishSubject<Boolean>

  /* Make sure to add all disposables to compositeDisposables to avoid memory leaks and crashes */
  protected val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    bindContentView(layoutId())

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      window.statusBarColor = StatusBarColor
    }
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

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
      when (it) {
        true -> uiUtils.showProgress()
        else -> uiUtils.hideProgress()
      }
    })

    /* handle exception */
    viewModel.exceptionLiveData.observe(this, Observer {
      it?.let { throwable -> errorUtils.handle(throwable) }
    })

    /* Observer network change state and show No-Internet UI, if internet is required by activity */
    connectionLiveData.observe(this, Observer {
      viewModel.isConnected = it ?: false
      if (requireConnection()) {
        runOnUiThread {
          if (it == true) {
            internetConnected()
          } else {
            internetDisconnected()
          }
        }
      }
    })

    /* ... other Ui observers */
  }

  /** Hide **/
  private fun bindContentView(layoutId: Int) {
    binding = DataBindingUtil.setContentView(this, layoutId)
    viewModel = ViewModelProviders.of(this, viewModelFactory)
        .get(getViewModelClass())
    binding.setVariable(BR.viewModel, viewModel)
  }

  override fun onDestroy() {
    super.onDestroy()

    //dispose and clear all process
    compositeDisposable.disposeAndClear()
  }

  /**
   * View Model Class
   *
   * @return [Class] of [VM]
   */
  abstract fun getViewModelClass(): Class<VM>

  /**
   * Layout Resource Id
   *
   * @return [LayoutRes] Layout Id
   */
  @LayoutRes
  protected abstract fun layoutId(): Int

  /**
   * Define whether internet is required by activity or not
   *
   * @return [Boolean] based on flag, active no internet snackbar will be shown
   */
  protected abstract fun requireConnection(): Boolean

  /**
   * Connected to internet callback
   */
  protected open fun internetConnected() {
    uiUtils.dismissSnackbar()
  }

  /**
   * Disconnected from internet callback
   */
  protected open fun internetDisconnected() {
    uiUtils.showNoInternetSnackbar()
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean =
    when (item?.itemId) {
      android.R.id.home -> {
        onBackPressed()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }

  /**
   * Request Permission as Single<Boolean>
   *
   * @param permission [Manifest.permission] as String
   * @return Single<Boolean> with result
   */
  @SuppressLint("NewApi")
  protected fun requestPermission(permission: String): Single<Boolean> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      return Single.just(true)
    }

    permissionResultSubject = PublishSubject.create()

    val permissionCode = ContextCompat.checkSelfPermission(this, permission)
    when (permissionCode) {
      PackageManager.PERMISSION_GRANTED -> {
        return Single.just(true)
      }
      else -> {
        ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQ_CODE)
      }
    }
    return permissionResultSubject.singleOrError()
  }

  /**
   * Handle OnRequestPermissionResult
   */
  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == PERMISSION_REQ_CODE) {
      if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
        permissionResultSubject.onNext(false)
      } else {
        permissionResultSubject.onNext(true)
      }
      permissionResultSubject.onComplete()
    }
  }
}

/* permission req code */
private const val PERMISSION_REQ_CODE = 9021