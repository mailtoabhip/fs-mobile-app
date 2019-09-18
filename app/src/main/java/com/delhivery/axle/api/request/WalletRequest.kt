package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

/**
 * Created by saurabhdhillon
 * for Delhivery Private Limited
 **
 *
 * Request creators for [WalletService]
 *
 **
 */
data class WalletUpdateRequest(
  @SerializedName("auto_withdraw") val autoWithdraw: Boolean
) {
  companion object {
    /**
     * @param autoWithdraw
     */
    fun getRequest(autoWithdraw: Boolean) = WalletUpdateRequest(autoWithdraw)
  }
}