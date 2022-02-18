package com.delhivery.axle.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.delhivery.axle.R
import com.delhivery.axle.config.UrlConfig
import com.delhivery.axle.databinding.ActivityHelpSupportBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class HelpSupportActivity : BaseActivity<ActivityHelpSupportBinding, HomeProfileViewModel>() {

    override fun getViewModelClass() = HomeProfileViewModel::class.java

    override fun layoutId() = R.layout.activity_help_support

    override fun requireConnection() = true

    @Inject lateinit var userPrefs:UserPrefs

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title =  "Help & Support"

        binding.paymentTerms.setOnClickListener {
            when (contactUtils.openURL("${UrlConfig.DashboardUrl.url()}/#/paymentterms")) {
                false -> uiUtils.showSnackbar("Could not open url")
            }
        }

        if((userPrefs.isLoadBoardClient || userPrefs.isLoadBoardSupplier)){
            if(viewModel.userPrefs.userMode.equals("post_load")){
                binding.paymentTerms.visibility = View.GONE
            }else{
                binding.paymentTerms.visibility = View.VISIBLE
            }
        }else{
            binding.paymentTerms.visibility = View.VISIBLE
        }
    }
}