package com.delhivery.axle.ui.fastag

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.response.FastagTransactionsByTollPlazaResponse
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import javax.inject.Inject

class FastagTransactionSelectionViewModel @Inject constructor(
    private val loadboardRepository: LoadboardRepository
) : BaseViewModel() {

    val transactionsByTollPlazaData = MutableLiveData<FastagTransactionsByTollPlazaResponse>()
    val progressData = MutableLiveData<Boolean>()

    fun getTransactionsByTollPlaza(
        tollPlazaId: String,
        dateTime: String? = null,
        fastagId: String? = null,
        limit: Int = 20,
        offset: Int = 0
    ) {
        progressData.value = true

        compositeDisposable += loadboardRepository.getFastagTransactionsByTollPlaza(
            tollPlazaId = tollPlazaId,
            dateTime = dateTime,
            fastagId = fastagId,
            limit = limit,
            offset = offset
        )
            .onBackground()
            .progress()
            .subscribe { _res, error ->
                progressData.value = false

                if (!error && _res != null) {
                    transactionsByTollPlazaData.value = _res
                } else {
                    error.handle()
                }
            }
    }
}
