package com.delhivery.axle.ui.fastag.qdr

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.response.DisputeIssuesResponse
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import javax.inject.Inject

class FastagDisputeIssuesViewModel @Inject constructor(
    private val loadboardRepository: LoadboardRepository
) : BaseViewModel() {

    val disputeIssuesData = MutableLiveData<DisputeIssuesResponse>()
    val errorData = MutableLiveData<String>()
    val progressData = MutableLiveData<Boolean>()

    fun getDisputeIssues(partner: String) {
        progressData.value = true

        compositeDisposable plusAssign loadboardRepository.getDisputeIssuesList(partner)
            .onBackground()
            .progress()
            .subscribe { _res, error ->
                progressData.value = false

                if (!error && _res != null) {
                    disputeIssuesData.value = _res
                } else {
                    error.handle()
                    errorData.value = error.message ?: "Failed to load dispute issues"
                }
            }
    }
}