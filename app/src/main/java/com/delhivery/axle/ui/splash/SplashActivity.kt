package com.delhivery.axle.ui.splash

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.text.TextUtils
import android.view.animation.OvershootInterpolator
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivitySplashBinding
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_ID
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_KEY
import com.delhivery.axle.ui.auth.AuthenticationActivity
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.HomeActivity
import com.delhivery.axle.ui.onboarding.OnboardingActivity
import com.delhivery.axle.ui.splash.SplashPostState.Auth
import com.delhivery.axle.ui.splash.SplashPostState.Home
import com.delhivery.axle.ui.splash.SplashPostState.Onboarding
import com.github.florent37.kotlin.pleaseanimate.please
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

class SplashActivity : BaseActivity<ActivitySplashBinding, SplashViewModel>() {
  init {
    StatusBarColor = Color.parseColor("#181818")
  }

  override fun getViewModelClass() = SplashViewModel::class.java

  override fun layoutId() = R.layout.activity_splash

  override fun requireConnection() = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    notificationId = intent?.extras?.getString(ARGS_NOTIFICATION_KEY) ?: ""
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* start splash animation */
    animate()
  }

  /**
   * Splash animation chain
   */
  private fun animate() {
    val isAuthenticated = viewModel.postState()
    please(1500, OvershootInterpolator()) {
      animate(binding.textDelhivery) toBe {
        alpha(1f)
      }
      animate(binding.imgLogo) toBe {
        alpha(1f)
        scale(1.6f, 1.6f)
      }
    }.withEndAction {
      checkForUpdatedVersion { it ->
        when (it) {
          true -> {
            dialogUtils.showBasicConfirmDialog(
                R.string.title_dialog_update,
                R.string.msg_dialog_update,
                positiveAction = "UPDATE",
                negativeAction = "CANCEL",
                positiveClickListener = {
                  it.dismiss()
                  openPlayStore()
                },
                negativeClickListener = {
                  it.dismiss()
                  finish()
                }
            )
          }
          false -> {
            postAnimate(isAuthenticated)
          }
        }
      }
    }
        .setStartDelay(SplashAnimationDelay / 2)
        .start()
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

            val pInfo = this.packageManager.getPackageInfo(packageName, 0)
            currentVersionCode = if (VERSION.SDK_INT >= VERSION_CODES.P) {
              pInfo.longVersionCode.toInt()
            } else {
              pInfo.versionCode
            }

            completedAction(playStoreVersionCode > currentVersionCode)
          } else {
            completedAction(false)
          }
        }
        .addOnFailureListener { completedAction(false) }
        .addOnCanceledListener { completedAction(false) }
  }

  private fun postAnimate(state: SplashPostState) {
    when (state) {
      Onboarding -> OnboardingActivity::class
      Auth -> AuthenticationActivity::class
      Home -> HomeActivity::class
    }.let {
      val bundle = Bundle()
      if (!TextUtils.isEmpty(notificationId)) {
        bundle.putString(ARGS_NOTIFICATION_ID, notificationId)
      }
      navigationUtils.navigate(it.java, true, bundle)
    }
  }
}

/* delay before animation starts */
private const val SplashAnimationDelay = 2000L