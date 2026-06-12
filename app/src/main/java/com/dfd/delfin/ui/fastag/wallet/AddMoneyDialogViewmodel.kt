package com.dfd.delfin.ui.fastag.wallet

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.WalletRepository
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.ui.dialogs.PaymentStatus
import com.dfd.delfin.utils.extensions.convertResponse
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import retrofit2.HttpException
import java.util.UUID
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

    fun initiateRecharge(amount: Float, deeplink: String) {
        val clRequestId = UUID.randomUUID().toString()
        compositeDisposable +=
            paymentRepository
                .initiateRecharge(amount, deeplink, clRequestId)
                .convertResponse()
                .progress()
                .onBackground()
                .subscribe({ response ->
                    handleRechargeSuccess(response)
                }, { error ->
                    if ((error as? HttpException)?.code() == 404) {
                        // Wallet doesn't exist — create it, then retry recharge
                        createWalletAndRetryRecharge(amount, deeplink)
                    } else {
                        error.handle()
                        rechargeInitLiveData.postValue(null)
                    }
                })
    }

    private fun createWalletAndRetryRecharge(amount: Float, deeplink: String) {
        compositeDisposable +=
            paymentRepository
                .createWallet()
                .convertResponse()
                .onBackground()
                .subscribe({ _ ->
                    // Wallet created — retry recharge
                    initiateRecharge(amount, deeplink)
                }, { error ->
                    error.handle()
                    rechargeInitLiveData.postValue(null)
                })
    }

    private fun handleRechargeSuccess(response: com.dfd.delfin.api.response.WalletRechargeInitResponse) {
        currentRechargeId = response.rechargeId
        rechargeStartDate = java.text.SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.S",
            java.util.Locale.getDefault()
        ).format(java.util.Date())
        val link = response.paymentLinkUrl
        val rechargeId = response.rechargeId
        if (link.isNotEmpty() && rechargeId.isNotEmpty()) {
            rechargeInitLiveData.postValue(Pair(link, rechargeId))
        } else {
            rechargeInitLiveData.postValue(null)
        }
    }

    /**
    * ── Step 2: Poll recharge status ──────────────────────────────────
    * Called by PaymentCountdownBottomSheetFragment on each polling tick.
    **/
    fun checkTransactionStatus(transactionId: String, startDate: String) {
        compositeDisposable +=
            paymentRepository
                .checkRechargeStatus(transactionId)
                .convertResponse()
                .onBackground()
                .subscribe({ response ->
                    val status =
                        when (response.status.lowercase()) {
                            "success" -> PaymentStatus.SUCCESS
                            "failed", "failure" -> PaymentStatus.FAILURE
                            else -> PaymentStatus.PENDING
                        }
                    paymentStatusLiveData.postValue(status)
                }, {
                    // On poll error, don't crash — leave status as PENDING
                    // so the countdown continues and retries next tick.
                    it.handle()
                })
    }
}
