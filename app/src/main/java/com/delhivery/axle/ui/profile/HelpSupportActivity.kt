package com.delhivery.axle.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.delhivery.axle.R
import com.delhivery.axle.config.UrlConfig
import com.delhivery.axle.databinding.ActivityHelpSupportBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.home.FragmentName
import com.delhivery.axle.utils.EVENT_CALL_VENDOR_DESK
import com.delhivery.axle.utils.PROPERTY_PAGE_NAME
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject

class HelpSupportActivity : BaseActivity<ActivityHelpSupportBinding, HomeProfileViewModel>() {

    override fun getViewModelClass() = HomeProfileViewModel::class.java

    override fun layoutId() = R.layout.activity_help_support

    override fun requireConnection() = true

    @Inject lateinit var userPrefs:UserPrefs
    private var activitySetupTrace: Trace? = null
    private var isFirstResume = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySetupTrace = FirebasePerformance.getInstance().newTrace("HelpSupportActivity_SetupTime")
        activitySetupTrace?.start()
    }
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title =  "Help & Support"

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                userPrefs.setPreviousScreen(this.javaClass.name)
                finish()
            }
        })

        binding.paymentTerms.setOnClickListener {
            when (contactUtils.openURL("${UrlConfig.DashboardUrl.url()}/#/paymentterms")) {
                false -> uiUtils.showSnackbar("Could not open url")
                else ->{}
            }
        }
        binding.terms.setOnClickListener {
            when (contactUtils.openURL("${UrlConfig.DashboardUrl.url()}/#/axle-app-conditions")) {
                false -> uiUtils.showSnackbar("Could not open url")
                else ->{}
            }
        }
        binding.privacy.setOnClickListener {
            when (contactUtils.openURL("${UrlConfig.DashboardUrl.url()}/#/axle-app-privacy-policy")) {
                false -> uiUtils.showSnackbar("Could not open url")
                else ->{}
            }
        }


        if(userPrefs.isLoadBoardClient== false || userPrefs.isLoadBoardSupplier == false) {
            binding.paymentTerms.visibility = View.VISIBLE
        }else{
            if(viewModel.userPrefs.userMode.equals("post_load")){
                binding.paymentTerms.visibility = View.GONE
            }else{
                binding.paymentTerms.visibility = View.VISIBLE
            }
        }

        binding.deleteLayout.setOnClickListener {
            confirmDelete()
        }

    }
    override fun onResume() {
        super.onResume()
        if (activitySetupTrace != null && isFirstResume) {
            activitySetupTrace?.stop()
            isFirstResume = false
        }
    }
    private fun confirmDelete() {
        dialogUtils.showBasicConfirmDialog(
                R.string.title_dialog_delete,
                R.string.msg_delete_account,
                positiveAction = "Delete",
                negativeAction = "BACK",
                positiveClickListener = {
                    it.dismiss()
                    viewModel.deleteAccountRequest()
                }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_call, menu)
        return true
    }
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.nav_call).isVisible = true
        menu.findItem(R.id.nav_filter).isVisible = false
        // menu.findItem(R.id.nav_filter).isVisible = false
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_call -> {
                //Capture Event
                analyticsUtil.trackEvent(
                    EVENT_CALL_VENDOR_DESK,
                    mutableListOf(PROPERTY_USER_ID, PROPERTY_PAGE_NAME),
                    mutableListOf(
                        userPrefs.userId(),
                        this.applicationContext.javaClass.simpleName
                    )
                )
                callHelpline()
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

}