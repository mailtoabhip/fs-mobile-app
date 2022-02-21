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
import android.view.LayoutInflater
import com.delhivery.axle.ui.kyc.address.AddressActivity


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
    var intent= Intent()
       //should be changed based on user_mode
        val userMode = userPrefs.userMode
        val kycSteps = if(userMode=="post_truck"){
      userPrefs.truckPostKyc.split(",").toTypedArray()
      }else{
      userPrefs.loadPostKyc.split(",").toTypedArray()
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
        activity.startActivity(intent)

    //finish activity, if required
    if (finishAfter) {
      activity.finish()
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
        showKycSubmittedDialog()

    }
  }

  fun getNavigationPercentage(currentStep: Int, totalStep: Int):Int{
    return (currentStep*100)/totalStep
   }

   fun getNavigationStepFormat(currentStep: Int, totalStep: Int):String{
     return "Step $currentStep of $totalStep"
   }

    fun showKycSubmittedDialog() {
        val dialog = Dialog(activity)
        val bindingDialog= DialogKycSubmittedBinding.inflate(activity.layoutInflater)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)
        dialog.show()
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
