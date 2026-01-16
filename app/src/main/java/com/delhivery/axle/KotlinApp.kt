package com.delhivery.axle

import com.delhivery.axle.injection.component.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.delhivery.axle.R.drawable
import com.delhivery.axle.utils.RootDetectionUtil
import com.delhivery.axle.utils.prefs.SecurityPrefs
import com.moengage.core.DataCenter
import com.moengage.core.MoEngage
import com.moengage.core.config.FcmConfig
import com.moengage.core.config.NotificationConfig
import androidx.work.Configuration
import androidx.work.WorkManager
import com.delhivery.axle.injection.module.DaggerWorkerFactory
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import java.io.InterruptedIOException
import javax.inject.Inject
import kotlin.system.exitProcess

/**
 * Kotlin Application, with application injector
 */
class KotlinApp : DaggerApplication() {
  override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
    DaggerAppComponent.factory().create(this)

  @Inject
  lateinit var workerFactory: DaggerWorkerFactory


  override fun onCreate() {
    super.onCreate()

    // CRITICAL: Check for rooted device before any initialization
    if (!checkDeviceRootAndContinue()) {
      return // Exit immediately if device is rooted
    }

    setupRxJavaErrorHandler()
    setupMoEngage()
    createNotificationChannel()
    WorkManager.initialize(
            this,
            Configuration.Builder()
                    .setWorkerFactory(workerFactory)
                    .build()
    )
  }

  /**
   * Check if device is secure (not rooted, not emulator).
   * Stores result in preferences for later UI handling.
   *
   * @return true to continue app initialization
   */
  private fun checkDeviceRootAndContinue(): Boolean {
    // Only check in release builds (not debug)
    if (BuildConfig.DEBUG) {
      Log.d(TAG, "Security checks skipped in debug build")
      return true //TODO true Currently enabled to check on Debug Build
    }

    try {
      val result = RootDetectionUtil.performFullSecurityCheck(this)

      if (result.isInsecure()) {
        handleInsecureDevice(result)
      } else {
        // Device is secure, save the result
        val securityPrefs = SecurityPrefs(this)
        securityPrefs.isDeviceRooted = false
        securityPrefs.rootCheckCompleted = true
        securityPrefs.lastRootCheckTimestamp = System.currentTimeMillis()
        Log.d(TAG, "Security check completed: Device is secure")
      }

      // Always continue to allow UI to show dialog
      return true
    } catch (e: Exception) {
      Log.e(TAG, "Error during security check, allowing app to continue", e)
      // On error, assume secure to avoid blocking legitimate users
      return true
    }
  }

  /**
   * Handle insecure device detection (rooted or emulator).
   * Logs the detection and saves to preferences.
   * UI will handle showing dialog and blocking.
   *
   * @param result Security check result with detection details
   */
  private fun handleInsecureDevice(result: RootDetectionUtil.SecurityCheckResult) {
    Log.w(TAG, "Insecure device detected: ${result.getSecurityStatus()}")

    // Log the detection for analytics
    logSecurityDetection(result)

    // Store the detection in preferences for UI to check
    val securityPrefs = SecurityPrefs(this)
    securityPrefs.isDeviceRooted = result.isRooted
    securityPrefs.isDeviceEmulator = result.isEmulator
    securityPrefs.rootCheckCompleted = true

    val detectionMethods = mutableListOf<String>()
    if (result.isRooted) {
      detectionMethods.add("Root: ${result.getRootDetectionMethodsString()}")
    }
    if (result.isEmulator) {
      detectionMethods.add("Emulator: ${result.getEmulatorDetectionMethodsString()}")
    }

    securityPrefs.rootDetectionMethods = detectionMethods.joinToString(" | ")
    securityPrefs.lastRootCheckTimestamp = System.currentTimeMillis()

    Log.w(TAG, "Security detection stored: ${securityPrefs.rootDetectionMethods}")
  }

  /**
   * Log security detection event to analytics.
   * Currently logs to Logcat. Can be extended to log to Firebase/MoEngage.
   *
   * @param result Security check result
   */
  private fun logSecurityDetection(result: RootDetectionUtil.SecurityCheckResult) {
    Log.w(TAG, "Security Detection Event - Status: ${result.getSecurityStatus()}, " +
            "Device: ${Build.MANUFACTURER} ${Build.MODEL}, " +
            "Android: ${Build.VERSION.RELEASE}, " +
            "App Version: ${BuildConfig.VERSION_NAME}")

    if (result.isRooted) {
      Log.w(TAG, "Root methods: ${result.getRootDetectionMethodsString()}")
    }
    if (result.isEmulator) {
      Log.w(TAG, "Emulator methods: ${result.getEmulatorDetectionMethodsString()}")
    }

    // TODO: Uncomment when analytics is ready
    // AnalyticsUtil.logEvent("security_threat_detected", mapOf(
    //     "is_rooted" to result.isRooted,
    //     "is_emulator" to result.isEmulator,
    //     "status" to result.getSecurityStatus(),
    //     "root_methods" to result.getRootDetectionMethodsString(),
    //     "emulator_methods" to result.getEmulatorDetectionMethodsString(),
    //     "device_model" to "${Build.MANUFACTURER} ${Build.MODEL}",
    //     "android_version" to Build.VERSION.RELEASE,
    //     "app_version" to BuildConfig.VERSION_NAME,
    //     "build_type" to BuildConfig.BUILD_TYPE,
    //     "flavor" to BuildConfig.FLAVOR
    // ))
  }

  /**
   * Check if device is secure (not rooted, not emulator).
   * Stores result in preferences for later UI handling.
   *
   * @return true to continue app initialization
   */
  private fun checkDeviceRootAndContinue(): Boolean {
    // Only check in release builds (not debug)
    if (BuildConfig.DEBUG) {
      Log.d(TAG, "Security checks skipped in debug build")
      return true //TODO true Currently enabled to check on Debug Build
    }

    try {
      val result = RootDetectionUtil.performFullSecurityCheck(this)

      if (result.isInsecure()) {
        handleInsecureDevice(result)
      } else {
        // Device is secure, save the result
        val securityPrefs = SecurityPrefs(this)
        securityPrefs.isDeviceRooted = false
        securityPrefs.rootCheckCompleted = true
        securityPrefs.lastRootCheckTimestamp = System.currentTimeMillis()
        Log.d(TAG, "Security check completed: Device is secure")
      }

      // Always continue to allow UI to show dialog
      return true
    } catch (e: Exception) {
      Log.e(TAG, "Error during security check, allowing app to continue", e)
      // On error, assume secure to avoid blocking legitimate users
      return true
    }
  }

  /**
   * Handle insecure device detection (rooted or emulator).
   * Logs the detection and saves to preferences.
   * UI will handle showing dialog and blocking.
   *
   * @param result Security check result with detection details
   */
  private fun handleInsecureDevice(result: RootDetectionUtil.SecurityCheckResult) {
    Log.w(TAG, "Insecure device detected: ${result.getSecurityStatus()}")

    // Log the detection for analytics
    logSecurityDetection(result)

    // Store the detection in preferences for UI to check
    val securityPrefs = SecurityPrefs(this)
    securityPrefs.isDeviceRooted = result.isRooted
    securityPrefs.isDeviceEmulator = result.isEmulator
    securityPrefs.rootCheckCompleted = true

    val detectionMethods = mutableListOf<String>()
    if (result.isRooted) {
      detectionMethods.add("Root: ${result.getRootDetectionMethodsString()}")
    }
    if (result.isEmulator) {
      detectionMethods.add("Emulator: ${result.getEmulatorDetectionMethodsString()}")
    }

    securityPrefs.rootDetectionMethods = detectionMethods.joinToString(" | ")
    securityPrefs.lastRootCheckTimestamp = System.currentTimeMillis()

    Log.w(TAG, "Security detection stored: ${securityPrefs.rootDetectionMethods}")
  }

  /**
   * Log security detection event to analytics.
   * Currently logs to Logcat. Can be extended to log to Firebase/MoEngage.
   *
   * @param result Security check result
   */
  private fun logSecurityDetection(result: RootDetectionUtil.SecurityCheckResult) {
    Log.w(TAG, "Security Detection Event - Status: ${result.getSecurityStatus()}, " +
            "Device: ${Build.MANUFACTURER} ${Build.MODEL}, " +
            "Android: ${Build.VERSION.RELEASE}, " +
            "App Version: ${BuildConfig.VERSION_NAME}")

    if (result.isRooted) {
      Log.w(TAG, "Root methods: ${result.getRootDetectionMethodsString()}")
    }
    if (result.isEmulator) {
      Log.w(TAG, "Emulator methods: ${result.getEmulatorDetectionMethodsString()}")
    }

    // TODO: Uncomment when analytics is ready
    // AnalyticsUtil.logEvent("security_threat_detected", mapOf(
    //     "is_rooted" to result.isRooted,
    //     "is_emulator" to result.isEmulator,
    //     "status" to result.getSecurityStatus(),
    //     "root_methods" to result.getRootDetectionMethodsString(),
    //     "emulator_methods" to result.getEmulatorDetectionMethodsString(),
    //     "device_model" to "${Build.MANUFACTURER} ${Build.MODEL}",
    //     "android_version" to Build.VERSION.RELEASE,
    //     "app_version" to BuildConfig.VERSION_NAME,
    //     "build_type" to BuildConfig.BUILD_TYPE,
    //     "flavor" to BuildConfig.FLAVOR
    // ))
  }

  /**
   * Setup global RxJava error handler to prevent crashes from UndeliverableExceptions
   * These occur when errors happen after the Observable stream has been disposed
   */
  private fun setupRxJavaErrorHandler() {
    RxJavaPlugins.setErrorHandler { throwable ->
      if (throwable is UndeliverableException) {
        val cause = throwable.cause
        if (cause is InterruptedIOException || cause is InterruptedException) {
          // Fine to ignore - happens when operations are cancelled (e.g., user navigates away)
          return@setErrorHandler
        }
        // Log other undeliverable exceptions
        if (BuildConfig.DEBUG) {
          throwable.printStackTrace()
        }
      } else {
        // Crash on other uncaught RxJava errors in debug mode
        if (BuildConfig.DEBUG) {
          throw throwable
        } else {
          // Log in production to prevent crashes
          throwable.printStackTrace()
        }
      }
    }
  }

  private fun setupMoEngage() {
    val isSdkEnabled = !(BuildConfig.FLAVOR == "development" || BuildConfig.FLAVOR == "uat")
    val moEngage = MoEngage.Builder(this, "965N4GFJCV9UF6OBEPETGZR3",DataCenter.DATA_CENTER_3)
        .configureNotificationMetaData(NotificationConfig(R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.color.colorPrimary,
          isMultipleNotificationInDrawerEnabled = true,
          isBuildingBackStackEnabled = false,
          isLargeIconDisplayEnabled = true
        ))
        .configureFcm(FcmConfig(false)) .build()
    MoEngage.initialiseDefaultInstance(moEngage)
  }
  private fun createNotificationChannel() {
    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
      val notificationChannel=NotificationChannel(CHANNEL_ID,"TOKEN SERVICE CHANNEL",NotificationManager.IMPORTANCE_DEFAULT)
      val manager = getSystemService(NotificationManager::class.java)
      manager.createNotificationChannel(notificationChannel)
    }

  }

  companion object {
    private const val TAG = "KotlinApp"
    const val CHANNEL_ID = "tokenServiceChannel"
  }
}