package com.dfd.delfin.ui.splash

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.dfd.delfin.BuildConfig
import com.dfd.delfin.R
import com.dfd.delfin.databinding.ActivitySplashBinding
import com.dfd.delfin.ui.accountdetails.AccountDetailsActivity
import com.dfd.delfin.ui.auth.AuthenticationActivity
import com.dfd.delfin.ui.base.BaseActivity
import com.dfd.delfin.ui.home.activity.home.HomeActivity
import com.dfd.delfin.ui.splash.SplashPostState.AccountDetails
import com.dfd.delfin.ui.splash.SplashPostState.Auth
import com.dfd.delfin.ui.splash.SplashPostState.Home
import com.dfd.delfin.utils.EVENT_APP_OPEN
import com.dfd.delfin.utils.PROPERTY_HOUR_OF_DAY
import com.dfd.delfin.utils.PROPERTY_USER_ID
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.SecurityPrefs
import com.dfd.delfin.utils.prefs.UserPrefs
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.system.exitProcess


/**
 * Splash screen
 */
class StartRoutingActivity : BaseActivity<ActivitySplashBinding, SplashViewModel>() {

  override fun getViewModelClass() = SplashViewModel::class.java

  override fun layoutId() = R.layout.activity_splash

  var latestCode :Int = 0
  var currentCode :Int =0
  var type :String = ""
  lateinit var splashScreen: SplashScreen
  var ifUpdateFalse=false
  private var activitySetupTrace: Trace? = null
  private var isFirstResume = true
  override fun requireConnection() = false
  @Inject lateinit var userPrefs: UserPrefs

  override fun onCreate(savedInstanceState: Bundle?) {
    splashScreen = installSplashScreen()
    super.onCreate(savedInstanceState)
    
    // CRITICAL: Check for rooted device and show blocking dialog if detected
    if (checkAndHandleRootedDevice()) {
      return // Root dialog shown, activity will be blocked
    }
    
    activitySetupTrace = FirebasePerformance.getInstance().newTrace("StartRoutingActivity_SetupTime")
    activitySetupTrace?.start()
    // Keep the splash screen visible for this Activity
    splashScreen.setKeepOnScreenCondition { true }
    //Capture event
    val cal = Calendar.getInstance()
    val currentHourIn24Format = cal[Calendar.HOUR_OF_DAY]
    analyticsUtil.moEngageTrackEvent(
            EVENT_APP_OPEN,
            mutableListOf(PROPERTY_USER_ID, PROPERTY_HOUR_OF_DAY),
            mutableListOf(userPrefs.userId() , currentHourIn24Format.toString())
    )
    isFirstResume = true
  }
  
  /**
   * Check if device is insecure (rooted or emulator) and show blocking dialog if necessary.
   * 
   * @return true if device is insecure and dialog was shown, false otherwise
   */
  private fun checkAndHandleRootedDevice(): Boolean {
    // Skip check in debug builds
    if (BuildConfig.DEBUG) {
      Log.d(TAG, "Security check skipped in debug build")
      return false
    }
    
    try {
      val securityPrefs = SecurityPrefs(this)
      
      // Check if security threat was detected during app initialization
      if (securityPrefs.isDeviceInsecure() && securityPrefs.rootCheckCompleted) {
        val isRooted = securityPrefs.isDeviceRooted
        val isEmulator = securityPrefs.isDeviceEmulator
        
        Log.w(TAG, "Insecure device detected: rooted=$isRooted, emulator=$isEmulator")
        showInsecureDeviceBlockingDialog(
          isRooted = isRooted,
          isEmulator = isEmulator,
          detectedMethods = securityPrefs.rootDetectionMethods ?: "Unknown"
        )
        return true
      }
      
      return false
    } catch (e: Exception) {
      Log.e(TAG, "Error checking security status, allowing app to continue", e)
      return false
    }
  }
  
  /**
   * Show a blocking dialog informing user that device is insecure.
   * App will exit when user dismisses the dialog.
   * 
   * @param isRooted Whether device is rooted
   * @param isEmulator Whether device is an emulator
   * @param detectedMethods String describing detection methods
   */
  private fun showInsecureDeviceBlockingDialog(
    isRooted: Boolean,
    isEmulator: Boolean,
    detectedMethods: String
  ) {
    val message = when {
      isRooted && isEmulator -> getString(R.string.root_and_emulator_detection_message)
      isRooted -> getString(R.string.root_detection_message_blocked)
      isEmulator -> getString(R.string.emulator_detection_message_blocked)
      else -> getString(R.string.root_detection_message_blocked)
    }
    
    AlertDialog.Builder(this)
        .setTitle(getString(R.string.security_warning_title))
        .setMessage(message)
        .setPositiveButton(getString(R.string.exit_app)) { dialog, _ ->
          Log.w(TAG, "User acknowledged security dialog, exiting app")
          
          // Log analytics event for user action
          logSecurityDialogDismissed(isRooted, isEmulator)
          
          dialog.dismiss()
          
          // Exit the app
          finishAffinity()
          exitProcess(0)
        }
        .setCancelable(false)
        .show()
    
    Log.w(TAG, "Security dialog shown. Detected by: $detectedMethods")
  }
  
  /**
   * Log when user dismisses the security detection dialog.
   */
  private fun logSecurityDialogDismissed(isRooted: Boolean, isEmulator: Boolean) {
    val threatType = when {
      isRooted && isEmulator -> "rooted_and_emulator"
      isRooted -> "rooted"
      isEmulator -> "emulator"
      else -> "unknown"
    }

    Log.w(TAG, "Security dialog dismissed by user. Threat: $threatType")

  }
  
  companion object {
    private const val TAG = "StartRoutingActivity"
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    animate()
    if(!userPrefs.hasLoggedIn) {
      compositeDisposable += requestPermission(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
        .onBackground()
        .subscribe { granted, error ->
          if (error == null && granted) {

          } else {
            Snackbar.make(
              binding.root,
              "Please enable notification",
              Snackbar.LENGTH_LONG
            ).setAction("Settings") {
              // Responds to click on the action
              val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
              val uri: Uri = Uri.fromParts("package", packageName, null)
              intent.data = uri
              startActivity(intent)
            }.show()
          }
        }
    }
    //binding.btnGetStarted.visibility = View.GONE
    binding.btnGetStarted.setOnClickListener {
      postAnimate(SplashPostState.Auth)
    }
  }

    override fun onResume() {
        super.onResume()
        if (activitySetupTrace != null && isFirstResume) {
            activitySetupTrace?.stop()
            isFirstResume = false
        }
    }
  /**
   * Splash animation chain
   *
   * Flow:
   * - Token present + profile pending → API call → Home or AccountDetails
   * - Token present + profile complete → Home directly
   * - No token (fresh install / cleared data) → show Get Started button, navigate to Auth on click
   */
  private fun animate() {
      viewModel.determineState()

      if (!viewModel.isUserLoggedIn()) {
        // No token — stay on routing screen with Get Started visible
        splashScreen.setKeepOnScreenCondition { false }
        binding.btnGetStarted.visibility = View.VISIBLE
      } else {
        // Logged in — wait for state (may need API call) and navigate directly
        lifecycleScope.launch {
          val resolvedState = viewModel.splashState.filterNotNull().first()
          splashScreen.setKeepOnScreenCondition { false }
          postAnimate(resolvedState)
        }
      }
  }
  private fun postAnimate(state: SplashPostState) {
    userPrefs.setPreviousScreen(this.javaClass.name)
    when (state) {
      Auth -> AuthenticationActivity::class
      Home -> HomeActivity::class
      AccountDetails -> AccountDetailsActivity::class
    }.let {
        navigationUtils.navigate(it.java, true)
    }
  }
}

/* delay before animation starts */
private const val SplashAnimationDelay = 2000L