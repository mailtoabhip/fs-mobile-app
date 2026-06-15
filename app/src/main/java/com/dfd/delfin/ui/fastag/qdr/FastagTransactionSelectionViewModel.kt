package com.dfd.delfin.ui.fastag.qdr

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.FastagRepository
import com.dfd.delfin.api.response.FastagTransactionsByTollPlazaResponse
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import javax.inject.Inject

class FastagTransactionSelectionViewModel @Inject constructor(
    private val fastagRepository: FastagRepository
) : BaseViewModel() {

    val transactionsByTollPlazaData = MutableLiveData<FastagTransactionsByTollPlazaResponse>()
    val progressData = MutableLiveData<Boolean>()

    fun getTransactionsByTollPlaza(
        tollPlazaId: String,
        dateTime: String? = null,
        fastagId: String? = null,
        limit: Int = 20,
        offset: Int = 0,
        txnId: String? = null
    ) {
        progressData.value = true

        compositeDisposable plusAssign fastagRepository.getFastagTransactionsByTollPlaza(
            tollPlazaId = tollPlazaId,
            dateTime = dateTime,
            fastagId = fastagId,
            limit = limit,
            offset = offset,
            txnId = txnId
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