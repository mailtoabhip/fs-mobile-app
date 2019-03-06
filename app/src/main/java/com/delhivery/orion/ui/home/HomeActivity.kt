package com.delhivery.orion.ui.home

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener
import android.support.v4.view.ViewCompat
import android.view.MenuItem
import com.delhivery.orion.R
import com.delhivery.orion.databinding.ActivityHomeBinding
import com.delhivery.orion.ui.base.BaseActivity
import com.delhivery.orion.ui.home.fragments.HomeBaseFragment
import com.delhivery.orion.ui.home.fragments.HomeFragmentType
import com.delhivery.orion.ui.home.fragments.HomeFragmentsAdapter
import com.delhivery.orion.utils.extensions.onPageSelected

class HomeActivity : BaseActivity<ActivityHomeBinding, HomeViewModel>(),
    OnNavigationItemSelectedListener {

  override fun getViewModelClass() = HomeViewModel::class.java

  override fun layoutId() = R.layout.activity_home

  override fun requireConnection() = true

  /* home fragments pager adapter */
  private val pagerAdapter: HomeFragmentsAdapter by lazy {
    HomeFragmentsAdapter(supportFragmentManager)
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = HomeFragmentType.BidsFragment.title

    /* setup view pager */
    binding.viewpager.apply {
      offscreenPageLimit = HomeFragmentType.count()
      adapter = pagerAdapter
      /* update ui on page changed */
      onPageSelected { p ->
        HomeFragmentType.pos(p)
            ?.let {
              title = it.title
              binding.bottomNav.selectedItemId = it.menuId
              observerToolbarElevation(p)
            }
      }
    }

    /* set navigation item selection listener */
    binding.bottomNav.setOnNavigationItemSelectedListener(this)

    /* by default observe first fragment */
    observerToolbarElevation(0)
  }

  /**
   * Observe toolbar from current fragment live Data or fallback to default
   */
  private fun observerToolbarElevation(pos: Int = 0) {
    val liveData: MutableLiveData<Float>? =
      (pagerAdapter.getItem(pos) as HomeBaseFragment).toolbarElevationLiveData
    if (liveData == null) {
      /* default toolbar elevation */
      ViewCompat.setElevation(binding.toolbar, resources.getDimension(R.dimen.toolbar_elevation))
    } else {
      liveData.observe(this, Observer {
        ViewCompat.setElevation(
            binding.toolbar,
            it ?: resources.getDimension(R.dimen.toolbar_elevation)
        )
      })
    }
  }

  override fun onNavigationItemSelected(item: MenuItem) = HomeFragmentType.posById(item.itemId)
      .let { pos ->
        binding.viewpager.apply {
          if (pos != -1 && currentItem != pos) {
            setCurrentItem(pos, true)
          }
        }
        pos != -1
      }
}