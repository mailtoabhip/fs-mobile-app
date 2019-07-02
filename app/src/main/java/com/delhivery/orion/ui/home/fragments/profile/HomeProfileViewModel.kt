package com.delhivery.orion.ui.home.fragments.profile

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.api.response.MonthlyEarning
import com.delhivery.orion.repository.TransactionsRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import javax.inject.Inject

class HomeProfileViewModel @Inject constructor(
  private val transactionsRepository: TransactionsRepository
) : BaseViewModel() {

  var tripEarningLiveData = MutableLiveData<List<MonthlyEarning>>()

  fun fetchTripMeter() {
    compositeDisposable += transactionsRepository.transactionTripMeter()
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error) {
            tripEarningLiveData.postValue(_res.tripEarningMap.toSortedMap().values.toMutableList())
          } else {
            error.handle()
          }
        }
  }
}