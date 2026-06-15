package com.dfd.delfin.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.dfd.delfin.R
import com.dfd.delfin.databinding.DialogVerifyGstOtpBinding
import com.dfd.delfin.ui.custom.DelfinOTPViewInterface
import com.dfd.delfin.ui.kyc.gst.GstVerificationActivity
import com.dfd.delfin.ui.kyc.gst.GstVerificationViewModel
import com.dfd.delfin.utils.DialogUtils
import com.dfd.delfin.utils.DialogUtilsInterface
import com.dfd.delfin.utils.UiUtils
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.safeDispose
import com.dfd.delfin.utils.prefs.UserPrefs
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ShowGstVerificationOtpDialog @Inject constructor(
        context: Context,
        private val dialogUtilsInterface: DialogUtilsInterface,
        private val uiUtils: UiUtils,
        private val phoneNumber: String,
        private val dialogUtils: DialogUtils,
        private val uploadText: String,
        private val viewModel: GstVerificationViewModel,
        private val gstVerificationActivity: GstVerificationActivity,
        private val userPrefs: UserPrefs,
        private val currSelectedGst:String

) : Dialog(context),DelfinOTPViewInterface {

    /* dialog binding */
    private lateinit var binding: DialogVerifyGstOtpBinding

    /* dismiss timeout disposable */
    private var timeoutDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setCancelable(true)
        /* dialog binding */
        binding = DialogVerifyGstOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.otpNum.text = viewModel.gstDetailData.value?.phoneNumber
        binding.otpView.otpViewInterface = this
        timerToResend()
        binding.closeBtn.setOnClickListener {
            dialogUtils.showVerifcationOptionsDialog(uploadText,dialogUtilsInterface )
            timeoutDisposable.safeDispose()
            dismiss()
        }

        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.attributes.windowAnimations = R.style.DialogAnimation
        window!!.setGravity(Gravity.BOTTOM)

        viewModel.otpVerified.observe(
                gstVerificationActivity, Observer {
            if(it){
                uiUtils.hideProgress()
                timeoutDisposable.safeDispose()
                this.dismiss()
                if(userPrefs.gstNumber.equals(currSelectedGst, ignoreCase = true) || userPrefs.gstNumber.isEmpty()){
                    viewModel.updateUserDetails()
                }else{
                    viewModel.resetKycDetails(userPrefs.retryVerification)
                }
            }else{
                uiUtils.hideProgress()
                binding.gstotpError.visibility =  View.VISIBLE
            }
        }
        )
    }

    override fun otpSubmitted(otp: CharArray) {
        binding.buttonShare.isEnabled = true
        binding.buttonShare.setOnClickListener {
            if(otp.joinToString("").length==4){
                binding.gstotpError.visibility =  View.GONE
                binding.buttonShare.isEnabled = false
                viewModel.verifyRequestOtp(otp)
            }else{
                binding.buttonShare.isEnabled = false
                binding.gstotpError.visibility =  View.VISIBLE
            }
        }
    }

    fun timerToResend() {
        timeoutDisposable = Observable.interval(0L, 1L, TimeUnit.SECONDS)
                .onBackground()
                .subscribe {
                    val timeLeft = 15L - it
                    if (timeLeft > 0) {
                        val f: NumberFormat = DecimalFormat("00")
                        binding.btnResendOtp.text = "${context.getString(R.string.label_resend_otp)} 00:"+ f.format(timeLeft!!)
                        binding.btnResendOtp.setTextColor(ContextCompat.getColor(context, R.color.color_hint))
                    } else if (timeLeft == 0L) {
                        binding.btnResendOtp.text = context.getString(R.string.label_resend_otp_done)
                        binding.btnResendOtp.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
                        binding.btnResendOtp.setOnClickListener {
                            viewModel.getRequestOtp(false)
                            timerToResend()
                        }
                    }
                }
    }

}