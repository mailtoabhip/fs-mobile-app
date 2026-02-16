package com.delhivery.axle.ui.fastag

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.response.FastagTransactionResponse
import com.delhivery.axle.api.response.FastagTransactionsByTollPlazaResponse
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import javax.inject.Inject

class FastagTransactionSelectionViewModel @Inject constructor(
    private val loadboardRepository: LoadboardRepository
) : BaseViewModel() {

    val transactionsData = MutableLiveData<FastagTransactionResponse>()
    val transactionsByTollPlazaData = MutableLiveData<FastagTransactionsByTollPlazaResponse>()
    val errorData = MutableLiveData<String>()
    val progressData = MutableLiveData<Boolean>()

    /**
     * NEW API - Get transactions by toll plaza ID
     * Currently using mock data from repository
     * When API is deployed, this will automatically use real data
     */
    fun getTransactionsByTollPlaza(
        tollPlazaId: String,
        dateTime: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ) {
        progressData.value = true

        compositeDisposable += loadboardRepository.getFastagTransactionsByTollPlaza(
            tollPlazaId = tollPlazaId,
            dateTime = dateTime,
            limit = limit,
            offset = offset
        )
            .onBackground()
            .subscribe { _res, error ->
                progressData.value = false

                if (error == null && _res != null) {
                    transactionsByTollPlazaData.value = _res
                } else {
                    errorData.value = error?.message ?: "Failed to load transactions"
                }
            }
    }
}
