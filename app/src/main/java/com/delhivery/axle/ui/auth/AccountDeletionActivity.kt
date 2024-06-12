package com.delhivery.axle.ui.auth

import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityDeletionAccountBinding
import com.delhivery.axle.ui.base.BaseActivity

class AccountDeletionActivity : BaseActivity<ActivityDeletionAccountBinding, AuthenticationViewModel>() {

  override fun getViewModelClass()=AuthenticationViewModel::class.java

  override fun layoutId()= R.layout.activity_deletion_account
  override fun requireConnection() = true
  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = "Account Deletion"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    binding.callVendorDesk.setOnClickListener {
      callHelpline()
    }

    binding.logout.setOnClickListener {
      viewModel.logout()
      navigationUtils.logout("Successfully logged out","fromUser")
    }
  }
}