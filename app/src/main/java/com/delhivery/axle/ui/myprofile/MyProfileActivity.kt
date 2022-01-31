package com.delhivery.axle.ui.myprofile

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityMyProfileBinding
import com.delhivery.axle.databinding.ActivityVerifyPanBinding

import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionsActivity
import com.delhivery.axle.ui.home.activity.transactionlist.transactionsIntent
import com.delhivery.axle.ui.home.fragments.profile.HomeProfileViewModel
import com.delhivery.axle.ui.kyc.gst.GstVerificationActivity
import com.delhivery.axle.utils.Config
import com.delhivery.axle.utils.EVENT_OTP_SEND
import com.delhivery.axle.utils.PROPERTY_MOBILE_NUMBER_ENTERED
import com.delhivery.axle.utils.extensions.actionDone
import com.delhivery.axle.utils.extensions.errorVibrate
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.raisedFocus
import kotlinx.android.synthetic.main.view_home_loads_progress_item.*


class MyProfileActivity  : BaseActivity<ActivityMyProfileBinding, HomeProfileViewModel>() {
    init {
        StatusBarColor = Color.parseColor("#ffffff")
    }

    override fun getViewModelClass() = HomeProfileViewModel::class.java

    override fun layoutId() = R.layout.activity_my_profile

    override fun requireConnection() = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }
}