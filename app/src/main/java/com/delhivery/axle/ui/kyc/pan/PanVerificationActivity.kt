package com.delhivery.axle.ui.kyc.pan

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityVerifyPanBinding
import com.delhivery.axle.fcm.ARGS_TRANSACTION_IDS

import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionsActivity
import com.delhivery.axle.ui.home.activity.transactionlist.transactionsIntent
import com.delhivery.axle.ui.kyc.aadhaar.AadhaarVerificationActivity
import com.delhivery.axle.ui.kyc.gst.GstVerificationActivity
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.actionDone
import com.delhivery.axle.utils.extensions.errorVibrate
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.raisedFocus
import kotlinx.android.synthetic.main.activity_verify_pan.*
import kotlinx.android.synthetic.main.view_home_loads_progress_item.*


class PanVerificationActivity  : BaseActivity<ActivityVerifyPanBinding, PanVerificationViewModel>() {
    init {
        StatusBarColor = Color.parseColor("#ededff")
    }
    override fun getViewModelClass() = PanVerificationViewModel::class.java

    override fun layoutId() = R.layout.activity_verify_pan

    override fun requireConnection() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            if(intent?.extras!=null){
                viewModel.currentStep = navigationUtils.getNavigationStepFormat(intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!, intent?.extras?.getInt(
                    TotalStepsKey)!!)
                progress.progress = navigationUtils.getNavigationPercentage(intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!,intent?.extras?.getInt(
                    TotalStepsKey)!!)
            }

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnVerifyPan.setOnClickListener {
            if(viewModel.panCardNumber.toCharArray().get(3).toLowerCase().equals("p")){
                viewModel.updatePanUserDetails()
            }else{
                viewModel.updateUserDetails()
            }

        }

        binding.editPan.apply {
            lengthAction(9){
               binding.btnVerifyPan.isEnabled = false
                binding.panVerifyProgress.visibility = View.GONE
                binding.textPanName.visibility = View.GONE
                binding.imgCorrect.visibility = View.GONE
                binding.editPan.error= false
            }

            lengthAction(10) {
                binding.editPan.isEnabled=false
                binding.panVerifyProgress.visibility = View.VISIBLE
                binding.textPanName.visibility = View.GONE
                viewModel.validatePAN()
            }

            }

           viewModel.validatePanLiveData.observe(
               this, Observer {
                       binding.editPan.isEnabled=true
                       binding.btnVerifyPan.isEnabled =true
                       binding.panVerifyProgress.visibility = View.GONE
                       binding.imgCorrect.visibility = View.VISIBLE
                       binding.textPanName.visibility = View.VISIBLE
                       binding.textPanName.text =  getString(R.string.msg_verified_pan_name, it.panHolderName)
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

