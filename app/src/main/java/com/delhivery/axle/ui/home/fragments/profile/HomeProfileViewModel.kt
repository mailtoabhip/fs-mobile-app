package com.delhivery.axle.ui.home.fragments.profile

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.response.MonthlyEarning
import com.delhivery.axle.repository.TransactionsRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import javax.inject.Inject

class HomeProfileViewModel @Inject constructor(
  private val transactionsRepository: TransactionsRepository
) : BaseViewModel() {

  var tripEarningLiveData = MutableLiveData<Map<Int, MonthlyEarning?>>()

  fun fetchTripMeter() {
    compositeDisposable += transactionsRepository.transactionTripMeter()
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error) {
            val earningMap = mutableMapOf<Int, MonthlyEarning?>().apply {
              put(1, _res.jan)
              put(2, _res.feb)
              put(3, _res.mar)
              put(4, _res.apr)
              put(5, _res.may)
              put(6, _res.jun)
              put(7, _res.jul)
              put(8, _res.aug)
              put(9, _res.sep)
              put(10, _res.oct)
              put(11, _res.nov)
              put(12, _res.dec)
            }
                .filter { it.value != null }
                .toSortedMap()
            tripEarningLiveData.postValue(earningMap)
          } else {
            error.handle()
          }
        }
  }
}
