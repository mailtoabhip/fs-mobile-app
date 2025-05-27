package com.delhivery.axle.ui.trucks

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityAddTruckBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.selectroute.activity.SelectRouteWelcomeIntentExtra
import com.delhivery.axle.ui.selectroute.activity.selectRouteIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject

class AddTruckPathwayActivity : BaseActivity<ActivityAddTruckBinding, TruckViewModel>() {

    override fun getViewModelClass()= TruckViewModel::class.java

    override fun layoutId() = R.layout.activity_add_truck

    override fun requireConnection() = true

    private var activitySetupTrace: Trace? = null
    private var isFirstResume = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySetupTrace = FirebasePerformance.getInstance().newTrace("AddTruckPathwayActivity_SetupTime")
        activitySetupTrace?.start()

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        binding.btnDoLater.setOnClickListener {
                navigationUtils.navigate(HomeActivity::class.java, true)
        }

        binding.btnAction.setOnClickListener {
            navigationUtils.navigateForActivityResult(
                intent = truckIntent(this@AddTruckPathwayActivity,source = VALUE_ADD_TRUCK_ONBOARDING_PAGE),
                requestCode = REQCODE_ADD_TRUCK
            )
        }
    }
    override fun onResume() {
        super.onResume()
        if (activitySetupTrace != null && isFirstResume) {
            activitySetupTrace?.stop()
            isFirstResume = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            REQCODE_ADD_TRUCK ->{
                navigationUtils.navigate(
                    HomeActivity::class.java, true
                )
            }
        }
    }


}
