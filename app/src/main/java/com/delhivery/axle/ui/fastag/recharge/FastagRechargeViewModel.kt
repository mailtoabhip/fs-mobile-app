package com.delhivery.axle.ui.fastag.recharge

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.FastagRepository
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.request.FastagRechargeRequest
import com.delhivery.axle.api.response.FastagRechargeResponse
import com.delhivery.axle.api.response.FastagStatusResponse
import com.delhivery.axle.api.service.WalletApiService
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.convertResponse
import com.delhivery.axle.utils.extensions.errorResponseBody
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.gson.JsonObject
import javax.inject.Inject

class FastagRechargeViewModel @Inject constructor(
    private val loadboardRepository: LoadboardRepository,
    private val fastagRepository: FastagRepository,
    private val walletApiService: WalletApiService,
    private val userPrefs: UserPrefs
) : BaseViewModel() {

    val walletBalanceData = MutableLiveData<Double>()
    val rechargeResponseData = MutableLiveData<FastagRechargeResponse>()
    val fastagStatusData = MutableLiveData<FastagStatusResponse>()
    val walletErrorData = MutableLiveData<String?>()

    fun fetchWalletDetails() {
        showProgress()
        compositeDisposable += walletApiService.fetchWalletInfo()
            .convertResponse()
            .onBackground()
            .subscribe({ response ->
                showProgress(false)
                userPrefs.walletId = response.walletId
                walletBalanceData.postValue(response.currentBalance.toDoubleOrNull() ?: 0.0)
            }, { error ->
                val errorBody = error.errorResponseBody()
                if (errorBody != null && errorBody.errorBody.errorCode() == 404) {
                    // Wallet not found — auto-create
                    createWallet()
                } else {
                    showProgress(false)
                    walletErrorData.postValue("Unable to fetch wallet details")
                }
            })
    }

    private fun createWallet() {
        val request = JsonObject().apply {
            addProperty("phone", userPrefs.phoneNumber?.replace("+91", "") ?: "")
        }
        compositeDisposable += walletApiService.createWallet(
            request = request
        )
            .convertResponse()
            .onBackground()
            .subscribe({ response ->
                showProgress(false)
                userPrefs.walletId = response.walletId
                walletBalanceData.postValue(response.currentBalance.toDoubleOrNull() ?: 0.0)
            }, {
                showProgress(false)
                walletErrorData.postValue("Unable to create wallet")
            })
    }

    fun rechargeFastag(fastagId: String, amount: Double, fastagStatus: String?, fastagBalance: String?) {
        showProgress()
        val request = FastagRechargeRequest(
            fastagId = fastagId,
            rechargeAmount = amount,
            fastagStatus = fastagStatus,
            fastagBalance = fastagBalance
        )

        compositeDisposable += fastagRepository.rechargeFastag(request)
            .onBackground()
            .subscribe({ response ->
                showProgress(false)
                rechargeResponseData.postValue(response)
                // Re-fetch wallet balance after recharge
                fetchWalletDetails()
            }, { error ->
                showProgress(false)
                error.handle()
            })
    }

    fun fetchFastagStatus(tagId: String) {
        compositeDisposable += fastagRepository.fetchFastagStatus(tagId)
            .onBackground()
            .subscribe({ response ->
                fastagStatusData.postValue(response)
            }, { error ->
                fastagStatusData.postValue(null)
            })
    }
}