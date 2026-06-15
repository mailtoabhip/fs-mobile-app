package com.dfd.delfin.ui.fastag.qdr

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.FastagRepository
import com.dfd.delfin.api.response.DisputeIssuesResponse
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import javax.inject.Inject

class FastagDisputeIssuesViewModel @Inject constructor(
    private val fastagRepository: FastagRepository
) : BaseViewModel() {

    val disputeIssuesData = MutableLiveData<DisputeIssuesResponse>()
    val errorData = MutableLiveData<String>()
    val progressData = MutableLiveData<Boolean>()

    fun getDisputeIssues(partner: String) {
        progressData.value = true

        compositeDisposable plusAssign fastagRepository.getDisputeIssuesList(partner)
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