package com.delhivery.axle.ui.kyc.pan

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityVerifyPanBinding

import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.kyc.gst.GstVerificationActivity
import com.delhivery.axle.ui.onboarding.BasicDetailsActivity
import com.delhivery.axle.ui.profile.MyProfileActivity
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.errorVibrate
import com.delhivery.axle.utils.extensions.focusClick
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject


class PanVerificationActivity  : BaseActivity<ActivityVerifyPanBinding, PanVerificationViewModel>() {
    init {
        StatusBarColor = Color.parseColor("#ededff")
    }
    @Inject
    lateinit var userPrefs: UserPrefs

    override fun getViewModelClass() = PanVerificationViewModel::class.java

    override fun layoutId() = R.layout.activity_verify_pan

    override fun requireConnection() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            if(intent?.extras!=null){
                viewModel.currentStep = navigationUtils.getNavigationStepFormat(intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!, intent?.extras?.getInt(
                    TotalStepsKey)!!)
            }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        userPrefs.retryVerificationOnBack=true
      if(userPrefs.retryVerification){
        navigationUtils.navigate(MyProfileActivity::class.java, true)
      }else {
        navigationUtils.navigate(BasicDetailsActivity::class.java, true)
      }
    }
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.progressStepLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navigationUtils.showProgressSteps(binding.progressStepLayout, 2)
        binding.btnVerifyPan.setOnClickListener {
                viewModel.updateUserDetails()

        }

        binding.editPan.apply {
            lengthAction(9){
               binding.btnVerifyPan.isEnabled = false
                binding.panVerifyProgress.visibility = View.GONE
                binding.textPanName.visibility = View.VISIBLE
                binding.imgCorrect.visibility = View.GONE
                binding.editPan.error= false
            }

            lengthAction(10) {
                binding.editPan.isEnabled=false
                binding.panVerifyProgress.visibility = View.VISIBLE
                binding.textPanName.visibility = View.VISIBLE
                viewModel.validatePAN()
            }

            }

        if(userPrefs.pancard.isNotNullOrEmpty()){
            viewModel.panCardNumber = userPrefs.pancard
        }else{
          binding.editPan.focusClick()
        }

           viewModel.validatePanLiveData.observe(
               this, Observer {
                       binding.editPan.isEnabled=true
                       binding.btnVerifyPan.isEnabled =true
                       binding.panVerifyProgress.visibility = View.GONE
                       binding.imgCorrect.visibility = View.VISIBLE
                       binding.textPanName.visibility = View.VISIBLE
                       binding.textPanName.text =  getString(R.string.msg_verified_pan_name, it.panHolderName)
                       userPrefs.panName= it.panHolderName!!
                       viewModel.panType = it.panCardType!!
               }
           )

        viewModel.userUpdateLiveData.observe(this, Observer {
            if (it) {
                val bundle = Bundle()
                bundle.putString(panKey,viewModel.panType)
                navigationUtils.checkNavigationKycStep(this,intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!,intent?.extras?.getInt(
                    TotalStepsKey)!!,bundle)
            } else {
                uiUtils.showSnackbar("Update Failed, Please try again")
            }
        })
        viewModel.errorLiveData.observe(
            this, Observer {
                it?.let { error ->
                    binding.panVerifyProgress.visibility = View.GONE
                    binding.editPan.isEnabled=true
                    binding.btnVerifyPan.isEnabled =false
                    binding.editPan.error= true

                    /* show error message in toast if not null || empty */
                    if (error.second.isNotNullOrEmpty()) {
                        uiUtils.showToast(error.second!!)
                    }
                    /* handle each error state */
                    when (error.first) {
                        AuthenticationUIError.InvalidPANNumber -> {   //Invalid pan number functionality
                            uiUtils.showToast(error.second!!)
                            binding.editPan.errorVibrate()
                        }

                        AuthenticationUIError.None -> {/* nothing */
                        }
                    }
                }
            }
        )
        }

    fun gstIntent(
            context: Context
    ): Intent = Intent(context, GstVerificationActivity::class.java).apply {

    }
    }
const val panKey = "pan"

