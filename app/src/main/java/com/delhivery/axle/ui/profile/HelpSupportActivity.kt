package com.delhivery.axle.ui.profile

import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityHelpSupportBinding
import com.delhivery.axle.ui.base.BaseActivity

class HelpSupportActivity : BaseActivity<ActivityHelpSupportBinding, HelpSupportViewModel>() {

    override fun getViewModelClass() = HelpSupportViewModel::class.java

    override fun layoutId() = R.layout.activity_help_support

    override fun requireConnection() = true

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

//        //setSupportActionBar(binding.toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        title =  "Help & Support"
    }
}