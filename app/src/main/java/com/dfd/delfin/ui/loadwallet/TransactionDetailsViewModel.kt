package com.dfd.delfin.ui.loadwallet

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.service.WalletApiService
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.utils.extensions.convertResponse
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.UserPrefs
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
