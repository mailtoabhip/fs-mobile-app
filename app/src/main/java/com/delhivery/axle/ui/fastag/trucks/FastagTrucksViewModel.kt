package com.delhivery.axle.ui.fastag.trucks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.FastagRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.response.FastagListingResponse
import com.delhivery.axle.api.response.FastagVehicle
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.prefs.UserPrefs
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FastagTrucksViewModel @Inject constructor(
    private val fastagRepository: FastagRepository,
    private val userRepository: UserRepository,
    private val userPrefs: UserPrefs
) : BaseViewModel() {

    private val _fastagTrucks = MutableLiveData<List<HomeTrucksRequestItemData>>()
    val fastagTrucks: LiveData<List<HomeTrucksRequestItemData>> = _fastagTrucks

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _balanceRefreshed = MutableLiveData<Pair<String, String>>()
    val balanceRefreshed: LiveData<Pair<String, String>> = _balanceRefreshed

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

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
                    val trucks = response.result?.map { it.toTruckItemData() } ?: emptyList()
                    _fastagTrucks.value = trucks
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

    /**
     * Maps FastagVehicle from listing API to HomeTrucksRequestItemData used by the UI adapter.
     */
    private fun FastagVehicle.toTruckItemData() = HomeTrucksRequestItemData(
        inventoryId = fastagId,
        vehicleNumber = fastagVrn ?: "",
        truckType = "closed",
        truckSize = "",
        truckUuid = "",
        capacity = 0.0,
        currentCityName = "",
        currentCityCode = "",
        unloadingDestination = "",
        unloadingDestinationCode = "",
        latestStatus = fastagTagStatus ?: "",
        latestUUID = "",
        originClusterId = "",
        destinationClusterId = "",
        sourcedAs = "",
        fastagTagId = fastagId,
        fastagVrn = fastagVrn,
        fastagBalance = fastagBalance,
        fastagIssuedBy = vehicleMake,
        fastagTagStatus = fastagTagStatus
    )
}
