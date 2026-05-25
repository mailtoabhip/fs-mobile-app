package com.delhivery.axle.ui.loadwallet

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.service.WalletApiService
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.convertResponse
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.google.gson.JsonObject
import javax.inject.Inject

class RechargeDetailsViewModel @Inject constructor(
    private val walletApiService: WalletApiService
) : BaseViewModel() {

    /** Pair(rechargeId, newStatus) on success, null on error */
    var refreshStatusLiveData = MutableLiveData<Pair<String, String>?>()
    var refreshErrorLiveData = MutableLiveData<String?>()

    fun fetchRechargeStatus(rechargeId: String, createdAt: String) {
        val request = JsonObject().apply {
            addProperty("recharge_id", rechargeId)
        }
        compositeDisposable += walletApiService.fetchRechargeStatus(request)
            .convertResponse()
            .onBackground()
            .subscribe({ result ->
                refreshStatusLiveData.postValue(Pair(result.rechargeId, result.status))
            }, {
                refreshErrorLiveData.postValue("Unable to refresh status")
            })
    }
}
