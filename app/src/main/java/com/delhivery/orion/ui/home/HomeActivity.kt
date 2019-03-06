package com.delhivery.orion.ui.home

import android.os.Bundle
import com.delhivery.orion.R
import com.delhivery.orion.databinding.ActivityHomeBinding
import com.delhivery.orion.ui.base.BaseActivity
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsFragment

class HomeActivity : BaseActivity<ActivityHomeBinding, HomeViewModel>() {
  override fun getViewModelClass() = HomeViewModel::class.java

  override fun layoutId() = R.layout.activity_home

  override fun requireConnection() = true

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = "Bids & Requests"

    supportFragmentManager.beginTransaction()
        .add(R.id.container, HomeBidsFragment())
        .commit()
  }
}