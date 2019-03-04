package com.delhivery.orion.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import java.util.regex.Pattern

/**
 * OTP Receiver
 */
class OTPReceiver(private val _interface: OTPReceiverInterface) : BroadcastReceiver() {

  override fun onReceive(
    p0: Context?,
    p1: Intent?
  ) {
    try {
      p1?.extras?.let { extras ->
        val smsBundle = extras.get(SMS_BUNDLE)
        val format = p1.getStringExtra(FORMAT)
        if (smsBundle != null && smsBundle is Array<*> && smsBundle.size > 0) {
          (smsBundle[0] as ByteArray?)?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && format != null) {
              SmsMessage.createFromPdu(it, format)
            } else {
              SmsMessage.createFromPdu(it)
            }?.let { parseOTP(it) }
          }
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  /**
   * Parse otp and publish results
   */
  private fun parseOTP(smsMessage: SmsMessage) {
    val pattern = Pattern.compile(OTP_REGEX)
    val matcher = pattern.matcher(smsMessage.messageBody)
    var otp: String? = null
    while (matcher.find()) {
      otp = matcher.group()
    }
    otp?.let { _interface.otpFound(it) }
  }
}

interface OTPReceiverInterface {
  /**
   * OTP found in incoming message
   */
  fun otpFound(otp: String)
}

private const val SMS_BUNDLE = "pdus"
private const val OTP_REGEX = "[0-9]{1,4}"
private const val FORMAT = "format"

/* otp intent filter */
const val OTP_INTENT_FILTER = "android.provider.Telephony.SMS_RECEIVED"