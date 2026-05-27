package com.delhivery.axle.ui.fastag.trucks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.prefs.UserPrefs
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FastagTrucksViewModel @Inject constructor(
    private val loadboardRepository: LoadboardRepository,
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
     * Fetch all trucks that have FASTag linked.
     * Uses the same inventory API but filters for trucks with fastag_id.
     */
    fun fetchFastagTrucks() {
        _isLoading.value = true
        _error.value = null

        // TODO: Replace mock data with actual API call
        // Mock data for UI testing
        _isLoading.value = false
        _fastagTrucks.value = getMockFastagTrucks()

//        val request = JsonObject().apply {
//            addProperty("user_id", userRepository.userId())
//            addProperty("limit", 100)
//            addProperty("offset", 0)
//            addProperty("has_fastag", true)
//        }
//
//        compositeDisposable.add(
//            loadboardRepository.getInventories(request)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ response ->
//                    _isLoading.value = false
//                    if (response.success) {
//                        val fastagTrucks = response.trucks.filter { it.fastagTagId != null }
//                        _fastagTrucks.value = fastagTrucks
//                    } else {
//                        _fastagTrucks.value = emptyList()
//                    }
//                }, { throwable ->
//                    _isLoading.value = false
//                    _error.value = throwable.message ?: "Failed to load FASTag trucks"
//                    _fastagTrucks.value = emptyList()
//                })
//        )
    }

    /**
     * Mock data for UI testing. Remove when API is ready.
     */
    private fun getMockFastagTrucks(): List<HomeTrucksRequestItemData> {
        return listOf(
            HomeTrucksRequestItemData(
                inventoryId = "inv_001",
                vehicleNumber = "DL01CA1234",
                truckType = "closed",
                truckSize = "32FTMXL",
                truckUuid = "truck_001",
                capacity = 14.0,
                currentCityName = "Delhi",
                currentCityCode = "DEL",
                unloadingDestination = "Mumbai",
                unloadingDestinationCode = "BOM",
                latestStatus = "Free",
                latestUUID = "uuid_001",
                originClusterId = "cluster_01",
                destinationClusterId = "cluster_02",
                sourcedAs = "FTL",
                fastagTagId = "TAG001",
                fastagVrn = "DL01CA1234",
                fastagBalance = "85",
                fastagIssuedBy = "Ashok Leyland",
                fastagTagStatus = "Active"
            ),
            HomeTrucksRequestItemData(
                inventoryId = "inv_002",
                vehicleNumber = "DL01CA5678",
                truckType = "open",
                truckSize = "22FT",
                truckUuid = "truck_002",
                capacity = 9.0,
                currentCityName = "Pune",
                currentCityCode = "PNQ",
                unloadingDestination = "Bangalore",
                unloadingDestinationCode = "BLR",
                ownership = "market_truck",
                latestStatus = "Free",
                latestUUID = "uuid_002",
                originClusterId = "cluster_03",
                destinationClusterId = "cluster_04",
                sourcedAs = "FTL",
                fastagTagId = "TAG002",
                fastagVrn = "DL01CA5678",
                fastagBalance = "85",
                fastagIssuedBy = "Ashok Leyland",
                fastagTagStatus = "Active"
            ),
            HomeTrucksRequestItemData(
                inventoryId = "inv_003",
                vehicleNumber = "DL01CA9012",
                truckType = "closed",
                truckSize = "32FTMXL",
                truckUuid = "truck_003",
                capacity = 14.0,
                currentCityName = "Hyderabad",
                currentCityCode = "HYD",
                unloadingDestination = "Chennai",
                unloadingDestinationCode = "MAA",
                ownership = "owns_truck",
                latestStatus = "Active",
                latestUUID = "uuid_003",
                originClusterId = "cluster_05",
                destinationClusterId = "cluster_06",
                sourcedAs = "FTL",
                fastagTagId = "TAG003",
                fastagVrn = "DL01CA9012",
                fastagBalance = "850",
                fastagIssuedBy = "Ashok Leyland",
                fastagTagStatus = "Active"
            )
        )
    }

    /**
     * Refresh FASTag balance for a specific tag.
     */
    fun refreshBalance(tagId: String) {
        compositeDisposable.add(
            loadboardRepository.getFastagBalance(tagId)
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
