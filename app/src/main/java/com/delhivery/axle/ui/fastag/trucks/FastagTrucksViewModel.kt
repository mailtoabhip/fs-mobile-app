package com.delhivery.axle.ui.fastag.trucks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.FastagRepository
import com.delhivery.axle.api.response.FastagVehicle
import com.delhivery.axle.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
        compositeDisposable.add(
            io.reactivex.Single.fromCallable {
                kotlinx.coroutines.runBlocking {
                    fastagRepository.getPendingActions()
                }
            }
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    _fastagPendingLoading.value = false
                    when (result) {
                        is com.delhivery.axle.api.repository.Resource.Success -> {
                            _fastagPendingCount.value = result.data?.count ?: 0
                        }
                        else -> _fastagPendingCount.value = 0
                    }
                }, {
                    _fastagPendingLoading.value = false
                    _fastagPendingCount.value = 0
                })
        )
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
