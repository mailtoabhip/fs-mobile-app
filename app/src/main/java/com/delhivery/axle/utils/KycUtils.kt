package com.delhivery.axle.utils

import com.delhivery.axle.utils.extensions.isNotNullOrEmpty

object KycUtils {

    fun getEncryptedAccountNo(accountNo : String) =
        if (accountNo.isNotNullOrEmpty()) {
            val encrypted = StringBuilder()
            val maskLength = (accountNo.length) - 4
            repeat((maskLength downTo 1).count()) { encrypted.append("*") }
            encrypted.append(accountNo.substring(maskLength))
            encrypted.toString()
        } else {
            "Not Available"
        }
}