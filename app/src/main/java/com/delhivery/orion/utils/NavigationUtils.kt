package com.delhivery.orion.utils

import android.content.Intent
import android.databinding.ViewDataBinding
import com.delhivery.orion.injection.scope.ActivityScope
import com.delhivery.orion.ui.base.BaseActivity
import com.delhivery.orion.ui.base.BaseFragment
import com.delhivery.orion.ui.base.BaseViewModel
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

/**
 * Navigation Utils, utility class helps for navigation among activity with other options
 */
@ActivityScope
class NavigationUtils @Inject constructor(private val activity: DaggerAppCompatActivity) {

  /**
   * Navigate to another activity
   *
   * @param anotherActivity Target activity class
   * @param finishAfter Should current activity be finished after navigation, default if false
   */
  fun <B : ViewDataBinding, VM : BaseViewModel, A : BaseActivity<B, VM>> navigate(
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
}