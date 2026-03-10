package com.delhivery.axle.ui.loadwallet

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import javax.inject.Inject

class RechargeDetailsViewModel @Inject constructor(
    private val loadboardRepository: LoadboardRepository
) : BaseViewModel() {

    /** Pair(rechargeId, newStatus) on success, null on error */
    var refreshStatusLiveData = MutableLiveData<Pair<String, String>?>()
    var refreshErrorLiveData = MutableLiveData<String?>()

    fun fetchRechargeStatus(rechargeId: String, createdAt: String) {
        val start = createdAt.replace('T', ' ').substringBefore('+').substringBefore('Z')
        compositeDisposable += loadboardRepository.fetchRechargeStatus(rechargeId, start)
            .onBackground()
            .subscribe({ result ->
                refreshStatusLiveData.postValue(Pair(result.rechargeId, result.status))
            }, {
                refreshErrorLiveData.postValue("Unable to refresh status")
            })
    }
}
