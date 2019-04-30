package com.delhivery.orion.utils

import android.content.Intent
import com.delhivery.orion.injection.scope.ActivityScope
import com.delhivery.orion.repository.AuthenticationRepository
import com.delhivery.orion.ui.auth.AuthenticationActivity
import com.delhivery.orion.ui.base.BaseActivity
import com.delhivery.orion.ui.base.BaseFragment
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
    finishAfter: Boolean = false
  ) {
    Intent(activity, anotherActivity).let {
      activity.startActivity(it)
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