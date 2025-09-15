package com.delhivery.axle.ui.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityInvalidBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.WindowInsetsUtils

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