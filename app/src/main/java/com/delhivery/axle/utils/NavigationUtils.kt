package com.delhivery.axle.utils

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import com.delhivery.axle.api.repository.AuthenticationRepository
import com.delhivery.axle.databinding.DialogKycSubmittedBinding
import com.delhivery.axle.injection.scope.ActivityScope
import com.delhivery.axle.ui.auth.AuthenticationActivity
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.base.BaseFragment
import com.delhivery.axle.ui.businessverification.BusinessVerificationActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.kyc.aadhaar.AadhaarVerificationActivity
import com.delhivery.axle.ui.kyc.address.CommunicationAddressActivity
import com.delhivery.axle.ui.kyc.gst.GstVerificationActivity
import com.delhivery.axle.ui.kyc.identityverification.IdentityVerificationActivity
import com.delhivery.axle.ui.kyc.pan.PanVerificationActivity
import com.delhivery.axle.ui.kyc.pan.panKey
import com.delhivery.axle.utils.prefs.UserPrefs
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ViewProgressStepsBinding
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsFragment
import com.delhivery.axle.ui.kyc.address.AddressActivity
import com.delhivery.axle.ui.onboarding.BasicDetailsActivity
import com.delhivery.axle.ui.paymentdetails.PaymentDetailsActivity
import com.delhivery.axle.ui.paymentdetails.VendorPolicyActivity
import com.delhivery.axle.ui.profile.MyProfileActivity
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import java.lang.Exception

/**
 * Navigation Utils, utility class helps for navigation among activity with other options
 */
@ActivityScope
class NavigationUtils @Inject constructor(
        private val activity: DaggerAppCompatActivity,
        private val authRepository: AuthenticationRepository,
        private val uiUtils: UiUtils
) {

  @Inject lateinit var userPrefs: UserPrefs
  /**
   * Navigate to another activity
   *
   * @param anotherActivity Target activity class
   * @param finishAfter Should current activity be finished after navigation, default if false
   */
  fun <A : BaseActivity<*, *>> navigate(
          anotherActivity: Class<A>,
          finishAfter: Boolean = false,
          extras: Bundle? = null
  ) {
    Intent(activity, anotherActivity).let {
      userPrefs.setPreviousScreen(activity.javaClass.name)
      if (extras != null) {
        it.putExtras(extras)
      }
      activity.startActivity(it)
    }

    //finish activity, if required
    if (finishAfter) {
      activity.finish()
    }
  }

  /**
   * Navigate to another activity
   *
   * @param intent Target intent
   * @param finishAfter Should current activity be finished after navigation, default if false
   */
  fun navigate(
          intent: Intent,
          finishAfter: Boolean = false,
          extras: Bundle? = null
  ) {
    intent.let {
      userPrefs.setPreviousScreen(activity.javaClass.name)
      if (extras != null) {
        it.putExtras(extras)
      }

      activity.startActivity(it)
    }

    //finish activity, if required
    if (finishAfter) {
      activity.finish()
    }
  }

  /**
   * Navigate to another activity with result callback
   *
   * @param intent Target activity intent
   * @param finishAfter Should current activity be finished after navigation, default if false
   */
  fun navigateForActivityResult(
          intent: Intent,
          finishAfter: Boolean = false,
          requestCode: Int,
          extras: Bundle? = null
  ) {
    intent.let {
      if (extras != null) {
        it.putExtras(extras)
      }
      activity.startActivityForResult(it, requestCode)
    }

    //finish activity, if required
    if (finishAfter) {
      activity.finish()
    }
  }

  /**
   * Add/Replace fragment
   */
  fun addReplaceFragment(
          containerId: Int,
          fragment: BaseFragment<*, *>,
          tag: String
  ) {
    activity.supportFragmentManager.apply {
      val _fragment = findFragmentByTag(tag)
      beginTransaction().apply {
        if (_fragment == null) {
          add(containerId, fragment, tag)
        } else {
          replace(containerId, fragment, tag)
        }
      }
          .commitNow()
    }
  }

  /**
   * Replace fragment
   */
  fun replaceFragment(
          containerId: Int,
          fragment: BaseFragment<*, *>,
          tag: String
  ) {
    activity.supportFragmentManager.apply {
      beginTransaction().apply {
        replace(containerId, fragment, tag)
      }
          .commitNow()
    }
  }

  /**
   * Logout and navigate to login screen
   */
  fun logout(message: String, intention: String = "notFromUser") {
    authRepository.logout(intention)
    uiUtils.showToast(message)

    val intent = Intent(activity, AuthenticationActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
    activity.startActivity(intent)
  }

  /**
   * In progress for KYC config
   *
   * Navigate to another activity based on config
   *
   * @param intent Target intent
   * @param finishAfter Should current activity be finished after navigation, default if false
   */
  fun navigateKyc(
          context: Context,
          finishAfter: Boolean = false,
          extras: Bundle
  ) {
    userPrefs.setPreviousScreen(context.javaClass.name)
    var intent= Intent()
       //should be changed based on user_mode
        val userMode = userPrefs.userMode
        var backToHome = false
        var retryDone  = false
        val kycSteps = if(userMode=="post_load"){
            userPrefs.loadPostKyc.split(",").toTypedArray()
      }else{
            userPrefs.truckPostKyc.split(",").toTypedArray()
       }
      if(userPrefs.retryVerification){
          if(userPrefs.retryVerificationOnBack){
            if(kycSteps.get(extras.getInt(StepKey))=="address"){
              if(userPrefs.addressRejectReason.isNotNullOrEmpty()){
                extras.putInt(StepKey,3)
              } else if(userPrefs.rcRejectReason.isNotNullOrEmpty()){
                extras.putInt(StepKey,2)
              } else if(userPrefs.identityRejectReason.isNotNullOrEmpty()){
                extras.putInt(StepKey,1)
              }else if(userPrefs.panRejectReason.isNotNullOrEmpty()) {
                extras.putInt(StepKey, 0)
              }else{
                backToHome=true
              }
            }else if(kycSteps.get(extras.getInt(StepKey))=="business"){
                  if(userPrefs.rcRejectReason.isNotNullOrEmpty()){
                      extras.putInt(StepKey,2)
                  } else if(userPrefs.identityRejectReason.isNotNullOrEmpty()){
                      extras.putInt(StepKey,1)
                  }else if(userPrefs.panRejectReason.isNotNullOrEmpty()) {
                      extras.putInt(StepKey, 0)
                  }else{
                      backToHome=true
                  }
              }else if(kycSteps.get(extras.getInt(StepKey))=="gst/aadhaar"){
                  if(userPrefs.identityRejectReason.isNotNullOrEmpty()){
                      extras.putInt(StepKey,1)
                  }else if(userPrefs.panRejectReason.isNotNullOrEmpty()) {
                      extras.putInt(StepKey, 0)
                  }else{
                       backToHome=true
                   }
              }else if(kycSteps.get(extras.getInt(StepKey))=="pan"){
                  if(userPrefs.panRejectReason.isNotNullOrEmpty()) {
                      extras.putInt(StepKey, 0)
                  }else{
                      backToHome=true
                  }
              }
              userPrefs.retryVerificationOnBack=false
          }else {
              if (userPrefs.panRejectReason.isNotNullOrEmpty()&& (userPrefs.panRejectReason.replace(" ", "").equals("Documentunderverification"))) {
                  extras.putInt(StepKey, 0)
              } else if (userPrefs.identityRejectReason.isNotNullOrEmpty() && !(userPrefs.identityRejectReason.replace(" ", "").equals("Documentunderverification"))) {
                  extras.putInt(StepKey, 1)
              }  else if (userPrefs.rcRejectReason.isNotNullOrEmpty() && !(userPrefs.rcRejectReason.replace(" ", "").equals("Documentunderverification"))) {
                  extras.putInt(StepKey, 2)
              } else if (userPrefs.addressRejectReason.isNotNullOrEmpty()&& !(userPrefs.addressRejectReason.replace(" ", "").equals("Documentunderverification"))) {
                extras.putInt(StepKey, 3)
              } else {
                retryDone=true
                userPrefs.verificationStatus="pending"
                navigate(MyProfileActivity::class.java,true)
              }
          }
      }

        if(kycSteps.get(extras.getInt(StepKey))=="pan") {
          intent = Intent(context, PanVerificationActivity::class.java)
        }else  if(kycSteps.get(extras.getInt(StepKey))=="gst/aadhaar"){
            if(userPrefs.pancard.toCharArray().get(3).toLowerCase().toString().equals("p") &&  userPrefs.isGstsByPanNotRegistered){
              intent= Intent(context, AadhaarVerificationActivity::class.java)
            }else if(userPrefs.pancard.toCharArray().get(3).toLowerCase().toString().equals("p") &&  !userPrefs.isGstsByPanNotRegistered){
                intent = Intent(context, GstVerificationActivity::class.java)
            }else{
                if(userPrefs.isGstsByPanNotRegistered) {
                    intent = Intent(context, IdentityVerificationActivity::class.java)
                }else{
                    intent = Intent(context, GstVerificationActivity::class.java)
                }
            }
        }else  if(kycSteps.get(extras.getInt(StepKey))=="address"){
           if(userPrefs.isGstsByPanNotRegistered){
               intent= Intent(context, CommunicationAddressActivity::class.java)
           }else{
               intent= Intent(context, AddressActivity::class.java)
           }

        }else  if(kycSteps.get(extras.getInt(StepKey))=="business"){
          intent= Intent(context, BusinessVerificationActivity::class.java)
        }
        val bundle = Bundle()
        bundle.putInt(TotalStepsKey, kycSteps.size)
        bundle.putInt(CurrentStepKey, extras.getInt(StepKey))
        intent.putExtras(bundle)
      Log.d("back",backToHome.toString())

      if(backToHome){
          backToHome=false
          navigate(MyProfileActivity::class.java,true)
      }else {
          if(!retryDone){
            try{
              activity.startActivity(intent)
              activity.finish()
            }catch (e:Exception){}

          }
      }

    //finish activity, if required
      if(retryDone){
          retryDone=false
      }else {
          if (finishAfter) {
              activity.finish()
          }
      }
  }

  fun checkNavigationKycStep(context: Context, currentStep: Int, totalStep: Int, extras: Bundle?){
    if(currentStep<totalStep){
      val bundle = Bundle()
      bundle.putInt(StepKey, (currentStep))
      if(extras?.getString(panKey) != null){
        bundle.putString(panKey, extras.getString(panKey))
      }
      this.navigateKyc(context, false, bundle)
    }else{
      navigateOnboardingSteps()

    }
  }

  fun navigateOnboardingSteps(fromHome: Boolean=false) {

    if(userPrefs.isLoadBoardClient== false || userPrefs.isLoadBoardSupplier == false) {
      //do nothing

    }
    else{

      if(!userPrefs.isUserVerfied){
        userPrefs.setPreviousScreen(activity.javaClass.name)
        if(userPrefs.getLanesPreference().isNullOrEmpty()&& userPrefs.truckTypes.isNullOrEmpty()){
          val intent = Intent(activity, BasicDetailsActivity::class.java)
          this.navigate(intent,true,null)
        }else if(userPrefs.pancard.isNullOrEmpty()) {
          val bundle = Bundle()
          bundle.putInt(StepKey, 0)
          this.navigateKyc(activity, fromHome, bundle)
        }else  if(!(userPrefs.aadhaarNumber.isNotNullOrEmpty() ||userPrefs.gstNumber.isNotNullOrEmpty() ||(userPrefs.cinNumber.isNotNullOrEmpty()||userPrefs.shopNumber.isNotNullOrEmpty()||userPrefs.udyogNumber.isNotNullOrEmpty()))){
          val bundle = Bundle()
          bundle.putInt(StepKey, 1)
          this.navigateKyc(activity, fromHome, bundle)
        }else  if(userPrefs.businessAddress.isNullOrEmpty()){
          val bundle = Bundle()
          bundle.putInt(StepKey, 2)
          this.navigateKyc(activity, fromHome, bundle)
        }else  if(!userPrefs.userMode.equals("post_load")){
          if( userPrefs.rcNumber.isNullOrEmpty() && !userPrefs.isTruckingDocumentUploaded){
            val bundle = Bundle()
            bundle.putInt(StepKey, 3)
            this.navigateKyc(activity, fromHome, bundle)
          }else if(userPrefs.ifscCode.isNullOrEmpty() || userPrefs.accNumber.isNullOrEmpty() || userPrefs.accNumber.equals("Not Available",true)){
            val intent = Intent(activity, PaymentDetailsActivity::class.java)
            this.navigate(intent,fromHome,null)
          }else if(!userPrefs.vendorPolicyAccepted){
            val intent = Intent(activity, VendorPolicyActivity::class.java)
            this.navigate(intent,fromHome,null)
          }else{
            if(!fromHome) {
              navigate(HomeActivity::class.java, true)
            }
          }

        }else if(userPrefs.ifscCode.isNullOrEmpty() || userPrefs.accNumber.isNullOrEmpty() || userPrefs.accNumber.equals("Not Available",true)){
          val intent = Intent(activity, PaymentDetailsActivity::class.java)
          this.navigate(intent,fromHome,null)

        }else if(!userPrefs.vendorPolicyAccepted){
          val intent = Intent(activity, VendorPolicyActivity::class.java)
          this.navigate(intent,fromHome,null)
        }else{
          if(!fromHome) {
            navigate(HomeActivity::class.java, true)
          }
        }

        }
      }
    }

  fun getNavigationPercentage(currentStep: Int, totalStep: Int):Int{
    return (currentStep*100)/totalStep
   }

   fun getNavigationStepFormat(currentStep: Int, totalStep: Int):String{
     return "Step $currentStep of $totalStep"
   }
   fun showProgressSteps(progressStepLayout: ViewProgressStepsBinding, step:Int) {

       when (step) {
           1 -> {
               progressStepLayout.step1.setImageDrawable(activity.resources.getDrawable(R.drawable.ic_current_step))
               progressStepLayout.step2.setImageDrawable(activity.resources.getDrawable(R.drawable.ic_incomplete_step))
               progressStepLayout.step3.setImageDrawable(activity.resources.getDrawable(R.drawable.ic_incomplete_step))
               progressStepLayout.line1.background = activity.resources.getDrawable(R.color.colorAccent)
               progressStepLayout.line2.background = activity.resources.getDrawable(R.color.light_line_grey)
               progressStepLayout.routeTxt.setTextColor(activity.resources.getColor(R.color.colorAccent))
               progressStepLayout.kycTxt.setTextColor(activity.resources.getColor(R.color.heading_black))
               progressStepLayout.paymentTxt.setTextColor(activity.resources.getColor(R.color.heading_black))
           }
           2 -> {
               progressStepLayout.step1.setImageDrawable(activity.resources.getDrawable(R.drawable.ic_completed_step))
               progressStepLayout.step2.setImageDrawable(activity.resources.getDrawable(R.drawable.ic_current_step))
               progressStepLayout.step3.setImageDrawable (activity.resources.getDrawable(R.drawable.ic_incomplete_step))
               progressStepLayout.line1.background = activity.resources.getDrawable(R.color.colorAccent)
               progressStepLayout.line2.background = activity.resources.getDrawable(R.color.light_line_grey)
               progressStepLayout.routeTxt.setTextColor(activity.resources.getColor(R.color.heading_black))
               progressStepLayout.kycTxt.setTextColor(activity.resources.getColor(R.color.colorAccent))
               progressStepLayout.paymentTxt.setTextColor(activity.resources.getColor(R.color.heading_black))
           }
           3 -> {
               progressStepLayout.step1.setImageDrawable(activity.resources.getDrawable(R.drawable.ic_completed_step))
               progressStepLayout.step2.setImageDrawable(activity.resources.getDrawable(R.drawable.ic_completed_step))
               progressStepLayout.step3.setImageDrawable(activity.resources.getDrawable(R.drawable.ic_current_step))
               progressStepLayout.line1.background = activity.resources.getDrawable(R.color.colorAccent)
               progressStepLayout.line2.background = activity.resources.getDrawable(R.color.colorAccent)
               progressStepLayout.routeTxt.setTextColor(activity.resources.getColor(R.color.heading_black))
               progressStepLayout.kycTxt.setTextColor(activity.resources.getColor(R.color.heading_black))
               progressStepLayout.paymentTxt.setTextColor(activity.resources.getColor(R.color.colorAccent))
           }
       }
   }
    fun showKycSubmittedDialog() {
        val dialog = Dialog(activity)
        val bindingDialog= DialogKycSubmittedBinding.inflate(activity.layoutInflater)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)
        if(userPrefs.isGstVerfied && userPrefs.isSameAsGst && userPrefs.isGstNotBypassed && userPrefs.userMode=="post_load"){
            bindingDialog.titleText.text = activity.resources.getString(R.string.kyc_verified_successfully)
            bindingDialog.titleSubText.text = activity.resources.getString(R.string.kyc_verified_complete_details)
        }else{
            bindingDialog.titleText.text = activity.resources.getString(R.string.details_submitted_successfully)
            bindingDialog.titleSubText.text = activity.resources.getString(R.string.notify_kyc_verification)
        }
        dialog.show()
       userPrefs.setPreviousScreen(VendorPolicyActivity::class.java.name)
        Handler().postDelayed({
            dialog.dismiss()
            this.navigate(HomeActivity::class.java, true)
        }, 2000)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}


const val StepKey = "step"
const val CurrentStepKey = "current_step"
const val TotalStepsKey = "total_steps"
