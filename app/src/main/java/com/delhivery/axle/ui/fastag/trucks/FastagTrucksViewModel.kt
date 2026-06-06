package com.delhivery.axle.ui.fastag.trucks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.delhivery.axle.api.repository.FastagRepository
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.response.FastagVehicle
import com.delhivery.axle.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import javax.inject.Inject

class FastagTrucksViewModel @Inject constructor(
    private val fastagRepository: FastagRepository
) : BaseViewModel() {

    private val _fastagTrucks = MutableLiveData<List<FastagVehicle>>()
    val fastagTrucks: LiveData<List<FastagVehicle>> = _fastagTrucks

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _balanceRefreshed = MutableLiveData<Pair<String, String>>()
    val balanceRefreshed: LiveData<Pair<String, String>> = _balanceRefreshed

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // ── FASTag Pending Actions ──────────────────────────────────────────

    private val _fastagPendingCount = MutableLiveData<Int>(0)
    val fastagPendingCount: LiveData<Int> = _fastagPendingCount

    private val _fastagPendingLoading = MutableLiveData<Boolean>(false)
    val fastagPendingLoading: LiveData<Boolean> = _fastagPendingLoading

    fun fetchFastagPendingCount() {
        _fastagPendingLoading.value = true
        viewModelScope.launch {
            when (val result = fastagRepository.getPendingActions()) {
                is Resource.Success -> {
                    _fastagPendingLoading.value = false
                    _fastagPendingCount.value = result.data?.count ?: 0
                }
                is Resource.Failure -> {
                    _fastagPendingLoading.value = false
                    _fastagPendingCount.value = 0
                }
                Resource.Loading -> { /* no-op */ }
            }
        }
    }

    /**
     * Fetch all vehicles that have FASTag linked using v2/fastag/listing API.
     */
    fun fetchFastagTrucks() {
        _isLoading.value = true
        _error.value = null

        compositeDisposable.add(
            fastagRepository.getFastagListing()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    _isLoading.value = false
                    _fastagTrucks.value = response.result ?: emptyList()
                }, { throwable ->
                    _isLoading.value = false
                    _error.value = throwable.message ?: "Failed to load FASTag trucks"
                    _fastagTrucks.value = emptyList()
                })
        )
    }

    /**
     * Refresh FASTag balance for a specific tag.
     */
    fun refreshBalance(tagId: String) {
        compositeDisposable.add(
            fastagRepository.getFastagBalance(tagId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    _balanceRefreshed.value = Pair(tagId, response.fastagBalance)
                }, { throwable ->
                    _error.value = "Failed to refresh balance"
                })
        )
    }
}
