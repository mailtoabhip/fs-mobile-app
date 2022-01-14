package com.delhivery.axle.ui.accountsetup

import android.graphics.Color
import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityAccountSetupBinding
import com.delhivery.axle.databinding.ActivityAuthenticationBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * Account setup screen
 */
class AccountSetupActivity : BaseActivity<ActivityAccountSetupBinding, AccountSetupViewModel>(){

  override fun getViewModelClass() = AccountSetupViewModel::class.java

  override fun layoutId() = R.layout.activity_account_setup

  override fun requireConnection() = true

  @Inject lateinit var userPrefs: UserPrefs

  init {
    StatusBarColor = Color.parseColor("#ededff")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    title = ""
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
  }

  override fun onBackPressed() {
  }

}