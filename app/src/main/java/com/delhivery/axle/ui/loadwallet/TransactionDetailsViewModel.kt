package com.delhivery.axle.ui.loadwallet

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import javax.inject.Inject

class TransactionDetailsViewModel @Inject constructor(
    private val loadboardRepository: LoadboardRepository
) : BaseViewModel() {

    /** Pair(txnId, newStatus) on success, null on error */
    var refreshStatusLiveData = MutableLiveData<Pair<String, String>?>()
    var refreshErrorLiveData = MutableLiveData<String?>()

    fun fetchTransactionStatus(txnId: String, createdAt: String) {
        val start = createdAt.substring(0, 10)

        compositeDisposable += loadboardRepository.fetchTransactionStatus(start, txnId)
            .onBackground()
            .subscribe({ result ->
                refreshStatusLiveData.postValue(Pair(result.txnId, result.status))
            }, {
                refreshErrorLiveData.postValue("Unable to refresh status")
            })
    }
}
