package com.dfd.delfin.ui.auth

import android.os.Bundle
import com.dfd.delfin.R
import com.dfd.delfin.databinding.ActivityInvalidBinding
import com.dfd.delfin.ui.base.BaseActivity
import com.dfd.delfin.utils.WindowInsetsUtils

class InvalidActivity : BaseActivity<ActivityInvalidBinding, AuthenticationViewModel>() {

    override fun getViewModelClass()=AuthenticationViewModel::class.java

    override fun layoutId()= R.layout.activity_invalid
    override fun requireConnection() = true
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        /* setup toolbar */
        setSupportActionBar(binding.toolbar)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }
        title = "Home"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dialogUtils.showErrorDialog("Supplier Not found",-3)
    }
}