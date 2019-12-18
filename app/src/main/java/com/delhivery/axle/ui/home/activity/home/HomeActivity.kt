package com.delhivery.axle.ui.home.activity.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityHomeBinding
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_ID
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.fragments.BaseHomeFragmentAction
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.home.fragments.HomeFragmentActionType
import com.delhivery.axle.ui.home.fragments.HomeFragmentType
import com.delhivery.axle.ui.home.fragments.HomeFragmentType.LoadsFragment
import com.delhivery.axle.ui.home.fragments.HomeFragmentsAdapter
import com.delhivery.axle.ui.home.fragments.NavigateHomeFragmentAction
import com.delhivery.axle.utils.extensions.onPageSelected
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener

/**
 * Home screen
 */
class HomeActivity : BaseActivity<ActivityHomeBinding, HomeViewModel>(),
    OnNavigationItemSelectedListener {

  override fun getViewModelClass() = HomeViewModel::class.java

  override fun layoutId() = R.layout.activity_home

  override fun requireConnection() = true

  /* home fragments pager adapter */
  private val pagerAdapter: HomeFragmentsAdapter by lazy {
    HomeFragmentsAdapter(supportFragmentManager)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    notificationId = intent?.extras?.getString(ARGS_NOTIFICATION_ID) ?: ""
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = "Load Requests"

    /* setup view pager */
    binding.viewpager.apply {
      offscreenPageLimit = HomeFragmentType.count()
      adapter = pagerAdapter
      /* update ui on page changed */
      onPageSelected { p ->
        HomeFragmentType.pos(p)
            ?.let {
              uiUtils.toggleKeyboard()
              this@HomeActivity.title = HomeFragmentType.pos(p)
                  ?.fragment?.title
              binding.bottomNav.selectedItemId = it.menuId
              observeFragmentLiveData(p)
            }
      }
    }

    binding.viewpager.disableScroll(true)

    /* set navigation item selection listener */
    binding.bottomNav.setOnNavigationItemSelectedListener(this)

    /* by default observe first fragment */
    observeFragmentLiveData()

    if (notificationId.isNotEmpty()) {
      markNotificationRead()
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_call, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    return when (item?.itemId) {
      R.id.nav_call -> {
        callHelpline()
        true
      }
      else -> {
        super.onOptionsItemSelected(item)
      }
    }
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    notificationId = intent?.extras?.getString(ARGS_NOTIFICATION_ID) ?: ""
    viewModel.fromNotification = true
    if (notificationId.isNotEmpty()) {
      markNotificationRead()
    }
    fragmentAction(NavigateHomeFragmentAction(LoadsFragment))
  }

  override fun markNotificationRead() {
    super.markNotificationRead()
    viewModel.markNotificationRead(notificationId)
  }

  private fun observeFragmentLiveData(pos: Int = 0) {
    val fragment = (pagerAdapter.getItem(pos) as HomeBaseFragment)
    val elevationLiveData: MutableLiveData<Float>? = fragment.toolbarElevationLiveData
    if (elevationLiveData == null) {
      /* default toolbar elevation */
      ViewCompat.setElevation(binding.toolbar, resources.getDimension(R.dimen.toolbar_elevation))
    } else {
      elevationLiveData.observe(this, Observer {
        ViewCompat.setElevation(
            binding.toolbar,
            it ?: resources.getDimension(R.dimen.toolbar_elevation)
        )
      })
    }
  }

  /**
   * Fragment action observer
   */
  fun fragmentAction(action: BaseHomeFragmentAction) {
    when (action.type) {
      /* navigate to fragment action */
      HomeFragmentActionType.Navigate -> {
        val fragmentType = (action as NavigateHomeFragmentAction).fragmentType
        binding.viewpager.setCurrentItem(fragmentType.position, true)
      }
    }
  }

  override fun onNavigationItemSelected(item: MenuItem) = HomeFragmentType.posById(item.itemId)
      .let { pos ->
        binding.viewpager.apply {
          uiUtils.toggleKeyboard()
          if (pos != -1 && currentItem != pos) {
            this@HomeActivity.title = HomeFragmentType.pos(pos)
                ?.fragment?.title
            setCurrentItem(pos, true)
          }
        }
        pos != -1
      }
}

/**
 * Provides title from all fragments to activity
 */
interface TitleProvider {
  val title: CharSequence
}