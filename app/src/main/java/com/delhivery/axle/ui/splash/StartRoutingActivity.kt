package com.delhivery.axle.ui.splash

import android.Manifest
import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.app.ProgressDialog.show
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivitySplashBinding
import com.delhivery.axle.fcm.ARGS_DEEPLINK_ID
import com.delhivery.axle.fcm.ARGS_DEEPLINK_TYPE
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_ID
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_KEY
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_TYPE
import com.delhivery.axle.fcm.ARGS_PREFERRED_TRANSACTION_ID
import com.delhivery.axle.fcm.ARGS_TRANSACTION_IDS
import com.delhivery.axle.fcm.ARGS_VEHICLE_NUMBER
import com.delhivery.axle.fcm.*
import com.delhivery.axle.ui.accountdetails.AccountDetailsActivity
import com.delhivery.axle.ui.auth.AuthenticationActivity
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsFragment
import com.delhivery.axle.ui.paymentdetails.VendorPolicyActivity
import com.delhivery.axle.ui.splash.SplashPostState.AccountDetails
import com.delhivery.axle.ui.splash.SplashPostState.Auth
import com.delhivery.axle.ui.splash.SplashPostState.Home
import com.delhivery.axle.utils.EVENT_APP_OPEN
import com.delhivery.axle.utils.EVENT_UPDATE_APP
import com.delhivery.axle.utils.EVENT_UPDATE_CANCEL
import com.delhivery.axle.utils.PROPERTY_CURRENT_VERSION
import com.delhivery.axle.utils.PROPERTY_HOUR_OF_DAY
import com.delhivery.axle.utils.PROPERTY_LATEST_VERSION
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.USER_PROPERTY_ANDROID_ID
import com.delhivery.axle.utils.USER_PROPERTY_ANDROID_VERSION
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import java.util.Calendar
import javax.inject.Inject

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
  override fun requireConnection() = false
  @Inject lateinit var userPrefs: UserPrefs
  private val APP_UPDATE_REQUEST_CODE = 1991

  private val appUpdateManager: AppUpdateManager by lazy {
    this.let { AppUpdateManagerFactory.create(it) }
  }

  private val appUpdatedListener: InstallStateUpdatedListener by lazy {
    object : InstallStateUpdatedListener {
      override fun onStateUpdate(installState: InstallState) {
        when {
          installState.installStatus() == InstallStatus.DOWNLOADED -> popupSnackbarForCompleteUpdate()
          installState.installStatus() == InstallStatus.INSTALLED -> appUpdateManager?.unregisterListener(this)
          else -> Log.d("InstallUpdatedListener", installState.installStatus()?.toString()?:"")
        }
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    splashScreen = installSplashScreen()
    super.onCreate(savedInstanceState)
    // Keep the splash screen visible for this Activity
    splashScreen.setKeepOnScreenCondition { true }
    //Capture event
    val cal = Calendar.getInstance()
    val currentHourIn24Format = cal[Calendar.HOUR_OF_DAY]
    analyticsUtil.trackEvent(
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


  }
  private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted: Boolean ->
    if (isGranted) {
      postAnimate(isAuthenticated)
    } else {
      // Explain to the user that the feature is unavailable because the
      // features requires a permission that the user has denied. At the
      // same time, respect the user's decision. Don't link to system
      // settings in an effort to convince the user to change their
      // decision.
    }
  }
  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* start splash animation */
    userPrefs.previousNavigationTab = HomeLoadsFragment::class.java.name
    userPrefs.currentNavigationTab = HomeLoadsFragment::class.java.name
    animate()
    checkForDynamicLinks()
    binding.btnGetStarted.visibility = View.GONE
    binding.btnGetStarted.setOnClickListener {
      if(ifUpdateFalse) {
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
          when {
            ContextCompat.checkSelfPermission(
              this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
              postAnimate(isAuthenticated)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
             /* Snackbar.make(
                findViewById(R.id.parent_layout),
                "Notification blocked",
                Snackbar.LENGTH_LONG
              ).setAction("Settings") {
                // Responds to click on the action
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
              }.show()*/
            }
            else -> {
              // The registered ActivityResultCallback gets the result of this request
              requestPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
              )
            }
          }
        }
        postAnimate(isAuthenticated)
      }else{
        postAnimate(isAuthenticated)
      }
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
  private fun checkForAppUpdate(forceUpdate:Boolean) {
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


  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == APP_UPDATE_REQUEST_CODE) {
      if (resultCode != Activity.RESULT_OK) {
        Toast.makeText(this, "App Update failed, please try again on the next app launch", Toast.LENGTH_SHORT).show() }
      }
    }


  private fun popupSnackbarForCompleteUpdate() {
    val snackbar = Snackbar.make(findViewById(android.R.id.content), "An update has just been downloaded.", Snackbar.LENGTH_INDEFINITE)
    snackbar.setAction("RESTART") { appUpdateManager.completeUpdate() }
    snackbar.show()
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
            checkForAppUpdate(false)
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
        Home -> VendorPolicyActivity::class
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