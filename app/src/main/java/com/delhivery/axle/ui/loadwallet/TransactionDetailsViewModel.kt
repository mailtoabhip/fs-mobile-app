package com.delhivery.axle.ui.loadwallet

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.service.WalletApiService
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.convertResponse
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class TransactionDetailsViewModel @Inject constructor(
    private val walletApiService: WalletApiService,
    private val userPrefs: UserPrefs
) : BaseViewModel() {

    /** Pair(txnId, newStatus) on success, null on error */
    var refreshStatusLiveData = MutableLiveData<Pair<String, String>?>()
    var refreshErrorLiveData = MutableLiveData<String?>()

    fun fetchTransactionStatus(txnId: String, createdAt: String) {
        compositeDisposable += walletApiService.fetchTransactions(
            userId = userPrefs.userId(),
            txnId = txnId,
            limit = 1
        )
            .convertResponse()
            .onBackground()
            .subscribe({ result ->
                val txn = result.transactions.firstOrNull()
                if (txn != null) {
                    refreshStatusLiveData.postValue(Pair(txn.transactionId, txn.status))
                } else {
                    refreshErrorLiveData.postValue("Unable to refresh status")
                }
            }, {
                refreshErrorLiveData.postValue("Unable to refresh status")
            })
    }
}
