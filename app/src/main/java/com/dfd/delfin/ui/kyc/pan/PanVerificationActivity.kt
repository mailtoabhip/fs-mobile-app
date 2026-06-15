package com.dfd.delfin.ui.kyc.pan

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import com.dfd.delfin.R
import com.dfd.delfin.R.string
import com.dfd.delfin.databinding.ActivityVerifyPanBinding
import com.dfd.delfin.ui.base.BaseActivity
import com.dfd.delfin.ui.kyc.gst.GstVerificationActivity
import com.dfd.delfin.utils.*
import com.dfd.delfin.utils.extensions.errorVibrate
import com.dfd.delfin.utils.extensions.focusClick
import com.dfd.delfin.utils.extensions.isNotNullOrEmpty
import com.dfd.delfin.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject

class PanVerificationActivity  : BaseActivity<ActivityVerifyPanBinding, PanVerificationViewModel>() {
    init {
        StatusBarColor = Color.parseColor("#ededff")
    }

    @Inject
    lateinit var userPrefs: UserPrefs
    var startTime: Long = 0
    var endTime: Long = 0

    override fun getViewModelClass() = PanVerificationViewModel::class.java

    override fun layoutId() = R.layout.activity_verify_pan

    override fun requireConnection() = false

    private var activitySetupTrace: Trace? = null
    private var isFirstResume = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySetupTrace = FirebasePerformance.getInstance().newTrace("PanVerificationActivity_SetupTime")
        activitySetupTrace?.start()
            if(intent?.extras!=null){
                viewModel.currentStep = navigationUtils.getNavigationStepFormat(intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!, intent?.extras?.getInt(
                    TotalStepsKey)!!)
            }
      onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            setResult(RESULT_CANCELED)
          finish()
        }
      })

    }

    fun trackEvent(ttl:String){
      analyticsUtil.moEngageTrackEvent(
          EVENT_CONFIRM_PAN,
          mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_TTL),
          mutableListOf(userPrefs.userId(), userPrefs.phoneNumber?:"dummy",ttl)
      )
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        startTime = System.currentTimeMillis()

        binding.btnVerifyPan.setOnClickListener {
                 if(viewModel.panCardNumber.equals(userPrefs.pancard, ignoreCase = true)|| userPrefs.pancard.isEmpty()){
                    viewModel.updateUserDetails()
                 }else{
                     /*First reset then update user details*/
                   viewModel.resetKycDetails(userPrefs.retryVerification)

                 }
        }

        binding.editPan.apply {
            lengthAction(9){
               binding.btnVerifyPan.isEnabled = false
                binding.panVerifyProgress.visibility = View.GONE
                binding.textPanName.visibility = View.VISIBLE
                binding.imgCorrect.visibility = View.GONE
                binding.editPan.error= false
              binding.textPanError.visibility=View.GONE
              binding.textPanNotLinkedToAadhaar.visibility=View.GONE
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
      viewModel.duplicatePanErrorLiveData.observe(this, Observer {
          binding.textPanError.visibility=View.VISIBLE
        binding.panVerifyProgress.visibility = View.GONE
        binding.editPan.isEnabled=true
        binding.btnVerifyPan.isEnabled =false
        binding.textPanName.text =  getString(R.string.hint_for_quick_processing)
        binding.editPan.error= true
      })

      viewModel.panNotLinkedToAadhaarErrorLiveData.observe(this) {
        binding.panVerifyProgress.visibility=View.GONE
        binding.editPan.isEnabled=true
        binding.btnVerifyPan.isEnabled=false
        binding.textPanName.text = getString(R.string.msg_verified_pan_name,it.second)
        binding.textPanNotLinkedToAadhaar.text = it.first
        binding.imgCorrect.visibility=View.VISIBLE
        binding.textPanNotLinkedToAadhaar.visibility=View.VISIBLE
      }
      binding.textPanError.setOnClickListener{
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:01246719699")
        startActivity(intent)
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
                endTime = System.currentTimeMillis()
                val ttl = endTime - startTime
                trackEvent(ttl.toString())
                finish()
                setResult(RESULT_OK)
            } else {
                uiUtils.showSnackbar(getString(string.error_update_failed))
            }
        })

      viewModel.resetKycLiveData.observe(this, Observer {
        if (it) {
                viewModel.updateUserDetails()
        } else {
          uiUtils.showSnackbar(getString(string.error_update_failed))
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

    override fun onResume() {
        super.onResume()
        if (activitySetupTrace != null && isFirstResume) {
            activitySetupTrace?.stop()
            isFirstResume = false
        }
    }

    fun gstIntent(
            context: Context
    ): Intent = Intent(context, GstVerificationActivity::class.java).apply {

    }
    }
const val panKey = "pan"

