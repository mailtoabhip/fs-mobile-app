package com.delhivery.axle.ui.fastag.wallet

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.request.WalletRechargeReqBody
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.dialogs.PaymentStatus
import com.delhivery.axle.utils.extensions.convertResponse
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import javax.inject.Inject

class AddMoneyDialogViewmodel @Inject constructor(
    private val paymentRepository: AddMoneyRepository
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

    // ── Step 1: Initiate recharge ─────────────────────────────────────────

    fun initiateRecharge(amount: Int, deeplink: String) {
        val request = WalletRechargeReqBody(
            amount      = amount,
            deeplinkUrl = deeplink
        )
        compositeDisposable += paymentRepository
            .initiateRecharge(request)
            .convertResponse()
            .progress()          // shows/hides BaseViewModel progress spinner
            .onBackground()
            .subscribe { response, error ->
                if (!error && response != null) {
                    rechargeInitLiveData.postValue(
                        Pair(response.paymentLink, response.rechargeId)
                    )
                } else {
                    error?.handle()   // posts to BaseViewModel.exceptionLiveData
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
}