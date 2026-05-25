package com.delhivery.axle.ui.fastag.recharge

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.WalletRepository
import com.delhivery.axle.api.request.FastagRechargeRequest
import com.delhivery.axle.api.response.FastagRechargeResponse
import com.delhivery.axle.api.response.FastagStatusResponse
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import javax.inject.Inject

class FastagRechargeViewModel @Inject constructor(
    private val loadboardRepository: LoadboardRepository,
    private val walletRepository: WalletRepository
) : BaseViewModel() {

    val walletBalanceData = MutableLiveData<Double>()
    val rechargeResponseData = MutableLiveData<FastagRechargeResponse>()
    val fastagStatusData = MutableLiveData<FastagStatusResponse>()

    fun fetchWalletDetails() {
        showProgress()
        compositeDisposable plusAssign loadboardRepository.fetchWalletDetails()
            .onBackground()
            .subscribe({ response ->
                showProgress(false)
                walletBalanceData.postValue(response.currentBalance)
            }, { error ->
                showProgress(false)
                walletBalanceData.postValue(0.0)
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

        compositeDisposable += loadboardRepository.rechargeFastag(request)
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
        compositeDisposable += loadboardRepository.fetchFastagStatus(tagId)
            .onBackground()
            .subscribe({ response ->
                fastagStatusData.postValue(response)
            }, { error ->
                fastagStatusData.postValue(null)
            })
    }
}