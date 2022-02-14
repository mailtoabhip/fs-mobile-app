package com.delhivery.axle.ui.profile

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityProfileDetailsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.biddetails.*

class ProfileDetailsActivity : BaseActivity<ActivityProfileDetailsBinding, ProfileDetailsViewModel>() {

    override fun getViewModelClass() = ProfileDetailsViewModel::class.java

    override fun layoutId() = R.layout.activity_profile_details

    override fun requireConnection() = true

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title =  "Profile Details"
    }
}

fun profileDetailsIntent(
    context: Context
) = Intent(context, ProfileDetailsActivity::class.java)