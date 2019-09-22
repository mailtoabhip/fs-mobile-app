package com.delhivery.axle.utils

import android.content.Intent
import android.os.Bundle
import com.delhivery.axle.injection.scope.ActivityScope
import com.delhivery.axle.repository.AuthenticationRepository
import com.delhivery.axle.ui.auth.AuthenticationActivity
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.base.BaseFragment
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

/**
 * Navigation Utils, utility class helps for navigation among activity with other options
 */
@ActivityScope
class NavigationUtils @Inject constructor(
  private val activity: DaggerAppCompatActivity,
  private val authRepository: AuthenticationRepository,
  private val uiUtils: UiUtils
) {

  /**
   * Navigate to another activity
   *
   * @param anotherActivity Target activity class
   * @param finishAfter Should current activity be finished after navigation, default if false
   */
  fun <A : BaseActivity<*, *>> navigate(
    anotherActivity: Class<A>,
    finishAfter: Boolean = false,
    extras: Bundle? = null
  ) {
    Intent(activity, anotherActivity).let {
      if (extras != null) {
        it.putExtras(extras)
      }
      activity.startActivity(it)
    }

    //finish activity, if required
    if (finishAfter) {
      activity.finish()
    }
  }

  /**
   * Navigate to another activity
   *
   * @param intent Target intent
   * @param finishAfter Should current activity be finished after navigation, default if false
   */
  fun navigate(
    intent: Intent,
    finishAfter: Boolean = false,
    extras: Bundle? = null
  ) {
    intent.let {
      if (extras != null) {
        it.putExtras(extras)
      }
      activity.startActivity(it)
    }

    //finish activity, if required
    if (finishAfter) {
      activity.finish()
    }
  }

  /**
   * Navigate to another activity with result callback
   *
   * @param intent Target activity intent
   * @param finishAfter Should current activity be finished after navigation, default if false
   */
  fun navigateForActivityResult(
    intent: Intent,
    finishAfter: Boolean = false,
    requestCode: Int,
    extras: Bundle? = null
  ) {
    intent.let {
      if (extras != null) {
        it.putExtras(extras)
      }
      activity.startActivityForResult(it, requestCode)
    }

    //finish activity, if required
    if (finishAfter) {
      activity.finish()
    }
  }

  /**
   * Add/Replace fragment
   */
  fun addReplaceFragment(
    containerId: Int,
    fragment: BaseFragment<*, *>,
    tag: String
  ) {
    activity.supportFragmentManager.apply {
      val _fragment = findFragmentByTag(tag)
      beginTransaction().apply {
        if (_fragment == null) {
          add(containerId, fragment, tag)
        } else {
          replace(containerId, fragment, tag)
        }
      }
          .commitNow()
    }
  }

  /**
   * Replace fragment
   */
  fun replaceFragment(
    containerId: Int,
    fragment: BaseFragment<*, *>,
    tag: String
  ) {
    activity.supportFragmentManager.apply {
      beginTransaction().apply {
        replace(containerId, fragment, tag)
      }
          .commitNow()
    }
  }

  /**
   * Logout and navigate to login screen
   */
  fun logout(message: String) {
    authRepository.logout()
    uiUtils.showToast(message)
    Intent(activity, AuthenticationActivity::class.java)
        .let {
          activity.startActivity(it)
        }
    activity.finish()
  }
}