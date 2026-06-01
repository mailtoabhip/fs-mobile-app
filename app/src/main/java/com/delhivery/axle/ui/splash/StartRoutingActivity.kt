package com.delhivery.axle.ui.splash

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.Secure
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivitySplashBinding
import com.delhivery.axle.fcm.ARGS_DEEPLINK_ID
import com.delhivery.axle.fcm.ARGS_DEEPLINK_TYPE
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_FROM
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_ID
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_KEY
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_TYPE
import com.delhivery.axle.fcm.ARGS_OFFER_ID
import com.delhivery.axle.fcm.ARGS_PREFERRED_TRANSACTION_ID
import com.delhivery.axle.fcm.ARGS_PRICING_ID
import com.delhivery.axle.fcm.ARGS_PRICING_SORT_KEY
import com.delhivery.axle.fcm.ARGS_TRANSACTION_IDS
import com.delhivery.axle.fcm.ARGS_VEHICLE_NUMBER
import com.delhivery.axle.ui.accountdetails.AccountDetailsActivity
import com.delhivery.axle.ui.auth.AuthenticationActivity
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.splash.SplashPostState.AccountDetails
import com.delhivery.axle.ui.splash.SplashPostState.Auth
import com.delhivery.axle.ui.splash.SplashPostState.Home
import com.delhivery.axle.utils.EVENT_APP_OPEN
import com.delhivery.axle.utils.PROPERTY_HOUR_OF_DAY
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.USER_PROPERTY_ANDROID_ID
import com.delhivery.axle.utils.USER_PROPERTY_ANDROID_VERSION
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.SecurityPrefs
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import java.util.Calendar
import javax.inject.Inject
import kotlin.system.exitProcess


/**
 * Splash screen
 */
class StartRoutingActivity : BaseActivity<ActivitySplashBinding, SplashViewModel>() {
  init {
    StatusBarColor = Color.parseColor("#181818")
  }

  override fun getViewModelClass() = SplashViewModel::class.java

  override fun layoutId() = R.layout.activity_splash


  var latestCode :Int = 0
  var currentCode :Int =0
  var type :String = ""
  var tid :String  = ""
  lateinit var splashScreen: SplashScreen
  lateinit var isAuthenticated :SplashPostState
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

    notificationId = intent?.extras?.getString(ARGS_NOTIFICATION_KEY) ?: ""
    transactions = intent?.extras?.getString(ARGS_TRANSACTION_IDS) ?: ""
    notificationType = intent?.extras?.getString(ARGS_NOTIFICATION_TYPE) ?: ""
    preferredTransactionId = intent?.extras?.getString(ARGS_PREFERRED_TRANSACTION_ID) ?: ""

     //For Inventory only
    vehicleNumber = intent?.extras?.getString(ARGS_VEHICLE_NUMBER) ?: ""

    //For pricing
    pricingId =  intent?.extras?.getString(ARGS_PRICING_ID) ?: ""
    pricingSortKey =  intent?.extras?.getString(ARGS_PRICING_SORT_KEY) ?: ""
    notificationFrom =  intent?.extras?.getString(ARGS_NOTIFICATION_FROM) ?: ""
    pricingOfferId =  intent?.extras?.getString(ARGS_OFFER_ID) ?: ""
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
    
    // TODO: Uncomment when analytics is ready
    // analyticsUtil.moEngageTrackEvent(
    //     "security_dialog_dismissed",
    //     mutableListOf("threat_type", "action"),
    //     mutableListOf(threatType, "exit")
    // )
  }
  
  companion object {
    private const val TAG = "StartRoutingActivity"
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    /* start splash animation */
  /*  userPrefs.previousNavigationTab = HomeLoadsFragment::class.java.name
    userPrefs.currentNavigationTab = HomeLoadsFragment::class.java.name*/
    animate()
    checkForDynamicLinks()
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
    binding.btnGetStarted.visibility = View.GONE
    binding.btnGetStarted.setOnClickListener {
      if(ifUpdateFalse) {
        postAnimate(isAuthenticated)
      }
    }
  }

    override fun onResume() {
        super.onResume()
        if (activitySetupTrace != null && isFirstResume) {
            activitySetupTrace?.stop()
            isFirstResume = false
        }
    }
  private fun checkForDynamicLinks() {
    FirebaseDynamicLinks.getInstance().getDynamicLink(intent)
            .addOnSuccessListener(this){
              Log.d("dynamicLinkFromSplash","Dynamic link Received")
              var deepLink: Uri? = null
              if (it != null) {
                deepLink = it.link
              }
              if(deepLink != null){
                Log.d("dynamicLinkFromSplash","Deep link Received" +deepLink.toString())
                type = deepLink.getQueryParameter("type")?:""
                tid = deepLink.getQueryParameter("id")?:""
                Log.d("dynamicLinkFromSplash","Deep link Parameters $tid $type")
              }
            }
            .addOnFailureListener(this){
              Log.d("dynamicLinkFromSplash", "getDynamicLink:onFailure")
            }
  }

  /**
   * Splash animation chain
   */
  private fun animate() {
     isAuthenticated = viewModel.postState()

      checkForUpdatedVersion { it ->
        when (it) {
          true -> {
            splashScreen.setKeepOnScreenCondition { false}
            checkForAppUpdate(true)
          }
          false -> {
            ifUpdateFalse=true
            if(ifUpdateFalse && userPrefs.hasLoggedIn){
              binding.btnGetStarted.visibility = View.GONE
              postAnimate(isAuthenticated)
            }else{
              splashScreen.setKeepOnScreenCondition { false }
              binding.btnGetStarted.visibility = View.VISIBLE
            }
          }
        }
      }

  }

  private fun openPlayStore() {
    val appPackageName = packageName
    try {
      startActivity(
          Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName"))
      )
    } catch (anfe: android.content.ActivityNotFoundException) {
      startActivity(
          Intent(
              Intent.ACTION_VIEW, Uri.parse(
              "https://play.google.com/store/apps/details?id=$appPackageName"
          )
          )
      )
    }
  }

  private fun checkForUpdatedVersion(completedAction: (success: Boolean) -> Unit) {
    val configSettings = FirebaseRemoteConfigSettings.Builder()
        .setMinimumFetchIntervalInSeconds(0)
        .build()

    val remoteConfig = FirebaseRemoteConfig.getInstance()
    remoteConfig.setConfigSettingsAsync(configSettings)

    FirebaseRemoteConfig.getInstance()
        .fetchAndActivate()
        .addOnCompleteListener(
            this
        ) {
          if (it.isSuccessful) {
            val currentVersionCode: Int
            val playStoreVersionCode: Int = try {
              remoteConfig.activate()
              remoteConfig.getString("android_latest_version_code")
                  .toInt()
            } catch (e: Exception) {
              0
            }
            val recommendedVersionCode: Int = try {
              remoteConfig.getString("recommended_update_version_code")
                .toInt()
            } catch (e: Exception) {
              0
            }

            try {
              viewModel.savePMTValidation(
                  remoteConfig.getString("max_pmt_rate").toInt(),
                  remoteConfig.getString("max_cost_per_km").toInt()
              )
            } catch (e: NumberFormatException) {
              //Do Nothing
            }
              try{
              viewModel.saveLoadPostKycConfig(
                  remoteConfig.getString("onboarding_order")
              )
                  viewModel.saveTruckPostKycConfig(
                      remoteConfig.getString("onboarding_order")
                  )
                viewModel.saveShareBannerH1Config(
                    remoteConfig.getString("advert_share_rate_banner_h1")
                )
                viewModel.saveShareBannerH2Config(
                    remoteConfig.getString("advert_share_rate_banner_h2")
                )
                viewModel.saveShareBannerH3Config(
                    remoteConfig.getString("advert_share_rate_banner_h3")
                )
                viewModel.savePodAddress(
                    remoteConfig.getString("pod_address")
                )
          } catch (e: Exception) {
            //Do Nothing
        }

            val pInfo = this.packageManager.getPackageInfo(packageName, 0)
            currentVersionCode = if (VERSION.SDK_INT >= VERSION_CODES.P) {
              pInfo.longVersionCode.toInt()
            } else {
              pInfo.versionCode
            }
            currentCode = currentVersionCode
            latestCode = playStoreVersionCode

            val androidId = Secure.getString(
              this.contentResolver,
              Secure.ANDROID_ID
            )
            //get device and app level details
            analyticsUtil.moEngageUserAttribute(USER_PROPERTY_ANDROID_ID,androidId)
            analyticsUtil.moEngageUserAttribute(USER_PROPERTY_ANDROID_VERSION,pInfo.versionName+"("+currentCode.toString()+")")
            viewModel.recommendedUpdate(
              recommendedVersionCode>currentVersionCode
            )
            completedAction(playStoreVersionCode > currentVersionCode)
          } else {
            completedAction(false)
          }
        }
        .addOnFailureListener { completedAction(false) }
        .addOnCanceledListener { completedAction(false) }
  }

  private fun postAnimate(state: SplashPostState) {
    /**
     * Check If it's from deep link
     * */
    userPrefs.setPreviousScreen(this.javaClass.name)
    if (state == Home && type != "") {
      val bundle = Bundle()
      bundle.putString(ARGS_DEEPLINK_TYPE , type)
      bundle.putString(ARGS_DEEPLINK_ID , tid)
     navigationUtils.navigate(HomeActivity::class.java, true, bundle)
    } else {
      when (state) {
        Auth -> AuthenticationActivity::class
        Home -> HomeActivity::class
        AccountDetails -> AccountDetailsActivity::class
      }.let {
        val bundle = Bundle()
        if (!TextUtils.isEmpty(notificationId)) {
          bundle.putString(ARGS_NOTIFICATION_ID, notificationId)
          bundle.putString(ARGS_NOTIFICATION_TYPE, notificationType)
          bundle.putString(ARGS_TRANSACTION_IDS, transactions)
          bundle.putString(ARGS_PREFERRED_TRANSACTION_ID, preferredTransactionId)
          //For Inventory
          bundle.putString(ARGS_VEHICLE_NUMBER, vehicleNumber)

          //For pricing
          bundle.putString(ARGS_PRICING_ID, pricingId)
          bundle.putString(ARGS_PRICING_SORT_KEY, pricingSortKey)
          bundle.putString(ARGS_OFFER_ID, pricingOfferId)
          bundle.putString(ARGS_NOTIFICATION_FROM, notificationFrom)

        }
          navigationUtils.navigate(it.java, true, bundle)
      }
    }
  }
}

/* delay before animation starts */
private const val SplashAnimationDelay = 2000L