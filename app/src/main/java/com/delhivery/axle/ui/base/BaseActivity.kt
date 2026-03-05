package com.delhivery.axle.ui.base

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.setMargins
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.delhivery.axle.BR
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.network.ConnectionLiveData
import com.delhivery.axle.utils.AnalyticsUtil
import com.delhivery.axle.utils.Config.AxleSupportEmail
import com.delhivery.axle.utils.ContactUtils
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.DialogUtils
import com.delhivery.axle.utils.EVENT_USER_LOGOUT
import com.delhivery.axle.utils.ErrorUtils
import com.delhivery.axle.utils.NavigationUtils
import com.delhivery.axle.utils.PROPERTY_TIME_SINCE_LAST_LOGIN
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.UiUtils
import com.delhivery.axle.utils.extensions.disposeAndClear
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
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

  /* set true if inline progress */
  protected var hasInlineProgress = false

  protected lateinit var binding: B
  protected lateinit var viewModel: VM

  @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
  @Inject lateinit var connectionLiveData: ConnectionLiveData
  @Inject lateinit var uiUtils: UiUtils
  @Inject lateinit var navigationUtils: NavigationUtils
  @Inject lateinit var errorUtils: ErrorUtils
  @Inject lateinit var analyticsUtil: AnalyticsUtil
  @Inject lateinit var dialogUtils: DialogUtils
  @Inject lateinit var contactUtils: ContactUtils
  var notificationId = ""
  var notificationType = ""
  var transactions = ""
  var transactionIds = listOf<String>()
  var preferredTransactionId = ""
  var vehicleNumber: String = ""
  var pricingId: String = ""
  var pricingSortKey: String = ""
  var notificationFrom: String = ""
  var pricingOfferId: String = ""

   val APP_UPDATE_REQUEST_CODE = 1991

  private lateinit var permissionResultSubject: PublishSubject<Boolean>

  /* Make sure to add all disposables to compositeDisposables to avoid memory leaks and crashes */
  protected val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }

  protected val appUpdateManager: AppUpdateManager by lazy {
    this.let { AppUpdateManagerFactory.create(it) }
  }

  protected val appUpdatedListener: InstallStateUpdatedListener by lazy {
    object : InstallStateUpdatedListener {
      override fun onStateUpdate(installState: InstallState) {
        when {
          installState.installStatus() == InstallStatus.DOWNLOADED -> popupSnackbarForCompleteUpdate()
          installState.installStatus() == InstallStatus.INSTALLED -> appUpdateManager.unregisterListener(this)
          else -> Log.d("InstallUpdatedListener", installState.installStatus().toString())
        }
      }
    }
  }

   open fun popupSnackbarForCompleteUpdate() {
    val snackbar = Snackbar.make(findViewById(android.R.id.content), "An update has just been downloaded.", Snackbar.LENGTH_INDEFINITE)
    snackbar.setAction("RESTART") { appUpdateManager.completeUpdate() }
    snackbar.show()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    
    // Enable edge-to-edge display for API 35+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
      WindowCompat.setDecorFitsSystemWindows(window, false)
    }
    
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

  override fun onResume() {
    super.onResume()
    appUpdateManager
      .appUpdateInfo
      .addOnSuccessListener { appUpdateInfo ->

        // If the update is downloaded but not installed,
        // notify the user to complete the update.
        if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
         popupSnackbarForCompleteUpdate()
        }

        //Check if Immediate update is required
        try {
          if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
            // If an in-app update is already running, resume the update.

            appUpdateManager.startUpdateFlowForResult(
              appUpdateInfo,
              AppUpdateType.IMMEDIATE,
              this,
              APP_UPDATE_REQUEST_CODE)

          }
        } catch (e: IntentSender.SendIntentException) {
          e.printStackTrace()
        }
      }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == APP_UPDATE_REQUEST_CODE) {
      if (resultCode != Activity.RESULT_OK) {
        Toast.makeText(this, "App Update failed, please try again on the next app launch", Toast.LENGTH_SHORT).show() }
    }
  }

  /** Hide **/
  private fun bindContentView(layoutId: Int) {
    binding = DataBindingUtil.setContentView(this, layoutId)
    viewModel = ViewModelProvider(this, viewModelFactory)
        .get(getViewModelClass())
    binding.setVariable(BR.viewModel, viewModel)
  }

  override fun onDestroy() {
    super.onDestroy()

    //dispose and clear all process
    compositeDisposable.disposeAndClear()
  }

  /**
   * Clears all pending notifications
   * and
   * Override this for marking notification received
   */
  open fun markNotificationRead() {
    with(NotificationManagerCompat.from(this)) {
      cancelAll()
    }
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
   * Returns true by default — override to disable for specific activities
   *
   * @return [Boolean] based on flag, active no internet snackbar will be shown
   */
  protected open fun requireConnection(): Boolean = true

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

  /**
   * Call helpline
   */
  fun callHelpline() {
    dialogUtils.showBasicConfirmDialog(
      R.string.title_support,
      R.string.msg_dialog_support,
      positiveAction = "Call",
      negativeAction = "Close",
      positiveClickListener = {
        compositeDisposable += requestPermission(arrayOf(Manifest.permission.CALL_PHONE))
          .onBackground()
          .subscribe { granted, error ->
            it.dismiss()
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
    )

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

  fun checkForAppUpdate(forceUpdate:Boolean) {
    // Returns an intent object that you use to check for an update.
    val appUpdateInfoTask = appUpdateManager.appUpdateInfo


    // Checks that the platform will allow the specified type of update.
    appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
      if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
        // Request the update.
        try {
          val installType = when {
            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) && !forceUpdate -> AppUpdateType.FLEXIBLE
            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) && forceUpdate -> AppUpdateType.IMMEDIATE
            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> AppUpdateType.IMMEDIATE
            else -> null
          }
          if (installType == AppUpdateType.FLEXIBLE) appUpdateManager.registerListener(appUpdatedListener)

          if (installType != null) {
            appUpdateManager.startUpdateFlowForResult(
              appUpdateInfo,
              installType,
              this,
              APP_UPDATE_REQUEST_CODE)

          }
        } catch (e: IntentSender.SendIntentException) {
          e.printStackTrace()
        }
      }
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean =
    when (item.itemId) {
      android.R.id.home -> {
        onBackPressedDispatcher.onBackPressed()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }

  /**
   * Check permission granted
   *
   * @param permissions [Manifest.permission] as array of String
   * @return Boolean
   */
  fun checkPermission(permissions: Array<String>): Boolean {
    for (permission in permissions) {
      val permCode = ContextCompat.checkSelfPermission(this, permission)
      if (permCode == PackageManager.PERMISSION_DENIED) {
        return false
      }
    }
    return true
  }

  /**
   * Request Permission as Single<Boolean>
   *
   * @param permissions [Manifest.permission] as array of String
   * @return Single<Boolean> with result
   */
  fun requestPermission(permissions: Array<String> = arrayOf()): Single<Boolean> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      return Single.just(true)
    }
    permissionResultSubject = PublishSubject.create()

    var permissionCode = PackageManager.PERMISSION_DENIED
    for (permission in permissions) {
      if(permission == Manifest.permission.WRITE_EXTERNAL_STORAGE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        continue
      permissionCode = ContextCompat.checkSelfPermission(this, permission)
      if (permissionCode == PackageManager.PERMISSION_DENIED) {
        break
      }
    }

    when (permissionCode) {
      PackageManager.PERMISSION_GRANTED -> {
        return Single.just(true)
      }
      else -> {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQ_CODE)
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