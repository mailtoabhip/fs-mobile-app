package com.delhivery.axle.ui.fastag

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.response.FastagTransactionResponse
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import okhttp3.ResponseBody
import javax.inject.Inject

class FastagTransactionDetailsViewModel @Inject constructor(
    private val loadboardRepository: LoadboardRepository
) : BaseViewModel() {

    val transactionsData = MutableLiveData<FastagTransactionResponse>()
    val errorData = MutableLiveData<String>()
    val progressData = MutableLiveData<Boolean>()
    val downloadData = MutableLiveData<ResponseBody>()

    private val pageSize = 20
    private var nextOffset: Int? = 0
    private var hasNext: Boolean = false
    private var currentTagId: String? = null

    fun loadTransactions(tagId: String, loadMore: Boolean = false) {
        // If loading more but no more data, return
        if (loadMore && !hasNext) {
            Log.d("FastagTransactions", "No more transactions to load")
            return
        }
        
        // Reset pagination if new tagId or initial load
        if (!loadMore || currentTagId != tagId) {
            currentTagId = tagId
            nextOffset = 0
            hasNext = false
        }
        
        val offset = nextOffset ?: 0
        progressData.value = true
        
        Log.d("FastagTransactions", "Loading transactions - tagId: $tagId, offset: $offset, pageSize: $pageSize, loadMore: $loadMore")

        compositeDisposable += loadboardRepository.getFastagTransactions(tagId, pageSize, offset)
            .onBackground()
            .subscribe({ response ->
                progressData.value = false
                Log.d("FastagTransactions", "Response received - status: ${response.status}, count: ${response.data?.count}, transactions: ${response.data?.transactions?.size}, next_offset: ${response.data?.nextOffset}, has_next: ${response.data?.hasNext}")
                
                // Update pagination state from API response
                hasNext = response.data?.hasNext ?: false
                nextOffset = response.data?.nextOffset
                
                transactionsData.value = response
            }, { error ->
                progressData.value = false
                Log.e("FastagTransactions", "Error loading transactions", error)
                errorData.value = error.message ?: "Failed to load transactions"
            })
    }
    
    fun downloadTransactions(tagId: String, fromDate: String, toDate: String) {
        progressData.value = true

        compositeDisposable += loadboardRepository.downloadFastagTransactions(
            tagId, fromDate, toDate
        )
            .onBackground()
            .subscribe({ responseBody ->
                progressData.value = false
                downloadData.value = responseBody
            }, { error ->
                progressData.value = false
                errorData.value = error.message ?: "Failed to download transactions"
            })
    }

}
