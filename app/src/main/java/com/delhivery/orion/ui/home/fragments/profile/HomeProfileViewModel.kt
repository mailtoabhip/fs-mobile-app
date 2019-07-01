package com.delhivery.orion.ui.home.fragments.profile

import com.delhivery.orion.repository.TransactionsRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import javax.inject.Inject

class HomeProfileViewModel @Inject constructor(
  private val transactionsRepository: TransactionsRepository
) : BaseViewModel() {

  fun fetchTripMeter() {
    compositeDisposable += transactionsRepository.transactionTripMeter()
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error) {

          } else {
            error.handle()
          }
        }
  }
}