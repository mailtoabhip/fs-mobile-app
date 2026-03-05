package com.delhivery.axle.ui.fastag.wallet

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.WalletRepository
import com.delhivery.axle.api.request.WalletRechargeReqBody
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.dialogs.PaymentStatus
import com.delhivery.axle.utils.extensions.convertResponse
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class AddMoneyDialogViewmodel @Inject constructor(
    private val paymentRepository: AddMoneyRepository,
    private val walletRepository: WalletRepository
) : BaseViewModel() {

    /**
    * Holds the payment link + recharge ID returned by initiateRecharge.
    * Observed by the Fragment to open the WebView.
    **/
    val rechargeInitLiveData = MutableLiveData<Pair<String, String>?>()  // paymentLink, rechargeId

    /**
    * Holds the resolved payment status (SUCCESS / FAILURE / PENDING).
    * Observed by PaymentCountdownBottomSheetFragment.
    **/
    val paymentStatusLiveData = MutableLiveData<PaymentStatus?>()

    var currentRechargeId: String = ""
    var rechargeStartDate: String = ""
    // ── Step 1: Initiate recharge ─────────────────────────────────────────

    fun initiateRecharge(amount: Int, deeplink: String) {
        val request = WalletRechargeReqBody(amount = amount, deeplinkUrl = deeplink)
        compositeDisposable += paymentRepository
            .initiateRecharge(request)
            .convertResponse()
            .progress()
            .onBackground()
            .subscribe { response, error ->
                if (!error && response != null) {
                    currentRechargeId = response.rechargeId
                    rechargeStartDate = SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss",
                        Locale.getDefault()
                    )
                        .format(Date())
                    rechargeInitLiveData.postValue(Pair(response.paymentLink, response.rechargeId))
                } else {
                    error?.handle()
                    rechargeInitLiveData.postValue(null)
                }
            }
    }

    /**
    * ── Step 2: Poll transaction status ──────────────────────────────────
    * Called by PaymentCountdownBottomSheetFragment on each polling tick.
    * The startDate is the timestamp when the recharge was initiated
    * (ISO format, e.g. "2026-02-25T10:30:00").
    **/

    fun checkTransactionStatus(transactionId: String, startDate: String) {
        compositeDisposable += paymentRepository
            .checkTransactionStatus(transactionId, startDate)
            .convertResponse()
            .onBackground()
            .subscribe { response, error ->
                if (!error && response != null) {
                    val status = when (response.status.lowercase()) {
                        "success"  -> PaymentStatus.SUCCESS
                        "failed",
                        "failure"  -> PaymentStatus.FAILURE
                        else       -> PaymentStatus.PENDING
                    }
                    paymentStatusLiveData.postValue(status)
                } else {
                    // On poll error, don't crash — leave status as PENDING
                    // so the countdown continues and retries next tick.
                    error?.handle()
                }
            }
    }

    /**
     * TODO Remove It
     * Call wallet recharge API
     */
    fun callWalletRecharge(amount: Int) {
        val walletId = "a2911006-7de6-11f0-95e2-06737cba16cc"
        //val amount = 1000 // Amount from curl example
        val redirectUrl = "https://www.delhivery.com" // Redirect URL from curl example
        val userName ="cb3914e8-cf95-4ae9-aa19-c6d0bf1ea7e5" // Get user ID
        val apiReqId = "1234567890" // Generate unique request ID

        // Call recharge API
        compositeDisposable += walletRepository.rechargeWallet(
            walletId = walletId,
            amount = amount,
            redirectUrl = redirectUrl,
            userName = userName,
            apiReqId = apiReqId
        )
            .onBackground()
            .progress()
            .subscribe { rechargeResponse, rechargeError ->
                if (rechargeResponse != null && !rechargeError && rechargeResponse.success) {
                    // Handle success - open payment URL in WebView with redirect URL
                    rechargeResponse.data?.data?.let {
                        rechargeInitLiveData.postValue(Pair(it.plodDetails?.link?:"", it.rechargeId?:""))
                    }

                } else {
                    val errorMessage = if (rechargeResponse != null && !rechargeResponse.success) {
                        rechargeResponse.message ?: "Failed to initiate recharge"
                    } else {
                        "Failed to initiate recharge. Please try again."
                    }
                }
            }
    }


}