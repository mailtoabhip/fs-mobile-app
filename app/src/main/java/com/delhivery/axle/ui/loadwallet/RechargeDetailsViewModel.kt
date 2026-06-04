package com.delhivery.axle.ui.loadwallet

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.FastagRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class RechargeDetailsViewModel @Inject constructor(
    private val fastagRepository: FastagRepository,
    private val userPrefs: UserPrefs
) : BaseViewModel() {

    /** Pair(rechargeId, newStatus) on success, null on error */
    var refreshStatusLiveData = MutableLiveData<Pair<String, String>?>()
    var refreshErrorLiveData = MutableLiveData<String?>()

    fun fetchRechargeStatus(rechargeId: String, createdAt: String) {
        compositeDisposable += fastagRepository.fetchRechargeStatus(
            userId = userPrefs.userId(),
            rechargeId = rechargeId
        )
            .onBackground()
            .subscribe({ result ->
                refreshStatusLiveData.postValue(Pair(result.rechargeId, result.status))
            }, {
                refreshErrorLiveData.postValue("Unable to refresh status")
            })
    }
}
