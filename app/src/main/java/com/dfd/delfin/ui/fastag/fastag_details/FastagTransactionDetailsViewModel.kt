package com.dfd.delfin.ui.fastag.fastag_details

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.FastagRepository
import com.dfd.delfin.api.response.FastagTransaction
import com.dfd.delfin.api.response.FastagTransactionResponse
import com.dfd.delfin.api.response.TransactionDisputeResponse
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.utils.extensions.errorPaymentResponseBody
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import okhttp3.ResponseBody
import javax.inject.Inject

class FastagTransactionDetailsViewModel @Inject constructor(
    private val fastagRepository: FastagRepository
) : BaseViewModel() {

    val transactionsData = MutableLiveData<FastagTransactionResponse>()
    val transactionDisputeData = MutableLiveData<TransactionDisputeResponse>()
    val errorData = MutableLiveData<String>()
    val progressData = MutableLiveData<Boolean>()
    val downloadData = MutableLiveData<ResponseBody>()

    private val pageSize = 20
    private var nextOffset: Int? = 0
    var hasNext: Boolean = false
        private set
    private var isLoading: Boolean = false
    private var currentTagId: String? = null
    private val allTransactions = mutableListOf<FastagTransaction>()

    fun loadTransactions(tagId: String, loadMore: Boolean = false) {
        // If loading more but no more data, return
        if (loadMore && !hasNext) {
            Log.d("FastagTransactions", "No more transactions to load")
            return
        }

        // Prevent duplicate calls while loading
        if (isLoading) return
        
        // Reset pagination if new tagId or initial load
        if (!loadMore || currentTagId != tagId) {
            currentTagId = tagId
            nextOffset = 0
            hasNext = false
            allTransactions.clear()
        }
        
        val offset = nextOffset ?: 0
        isLoading = true
        progressData.value = true
        

        compositeDisposable += fastagRepository.getFastagTransactions(tagId, pageSize, offset)
            .onBackground()
            .progress()
            .subscribe{ _res, error ->
                isLoading = false
                progressData.value = false
                
                if(!error && _res != null) {

                    // Update pagination state from API response
                    hasNext = _res.hasNext ?: false
                    nextOffset = _res.nextOffset
                    
                    // Accumulate transactions across pages
                    _res.transactions?.let { allTransactions.addAll(it) }
                    
                    transactionsData.value = _res.copy(transactions = ArrayList(allTransactions))
                } else {
                    error.handle()
                    errorData.value = error.message ?: "Failed to load transactions"
                }
            }
    }
    
    fun downloadTransactions(tagId: String, fromDate: String, toDate: String) {
        progressData.value = true

        compositeDisposable += fastagRepository.downloadFastagTransactions(
            tagId, fromDate, toDate
        )
            .onBackground()
            .progress()
            .subscribe{ _res, error ->
                progressData.value = false
                
                if(!error && _res != null) {
                    downloadData.value = _res
                } else {
                    val errorBody = error.errorPaymentResponseBody()?.errorBody
                    val errorMessage = if (errorBody != null && errorBody.code() == 404) {
                        "No recent transaction"
                    } else {
                        error.message ?: "Failed to download transactions"
                    }
                    errorData.value = errorMessage
                    error.handle()
                }
            }
    }

    fun getTransactionDispute(txnId: String) {
        progressData.value = true

        compositeDisposable += fastagRepository.getDisputeIssues(txnId)
            .onBackground()
            .progress()
            .subscribe { _res, error ->
                progressData.value = false

                if (!error && _res != null) {
                    transactionDisputeData.value = _res
                } else {
                    error.handle()
                    errorData.value = error.message ?: "Failed to load transaction details"
                }
            }
    }

}
