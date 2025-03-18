package com.delhivery.axle.ui.placementdetails

import android.text.BoringLayout
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.BidsRepository
import com.delhivery.axle.api.repository.InventoryRepository
import com.delhivery.axle.api.repository.TPSRepository
import com.delhivery.axle.api.repository.TransactionsRepository
import com.delhivery.axle.api.repository.UserTrucksLoadLimit
import com.delhivery.axle.api.request.UpdateVehicleDetailsRequest
import com.delhivery.axle.api.response.FacilityAddressResponse
import com.delhivery.axle.api.response.TPSErrorBody
import com.delhivery.axle.data.home.placements.HomePlacementsItemData
import com.delhivery.axle.data.home.trucks.HomeTrucksInfoItemData
import com.delhivery.axle.data.home.trucks.HomeTrucksPriorityItemData
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.contractDetails.ContractDetailsCreateEditDialogInterface
import com.delhivery.axle.ui.home.fragments.trucks.BaseHomeTrucksRVAdapterItem
import com.delhivery.axle.ui.home.fragments.trucks.HomeTruckPriorityAccessItem
import com.delhivery.axle.ui.home.fragments.trucks.HomeTrucksFilterItem
import com.delhivery.axle.ui.home.fragments.trucks.HomeTrucksInfoItem
import com.delhivery.axle.ui.home.fragments.trucks.HomeTrucksProgressItem
import com.delhivery.axle.ui.home.fragments.trucks.HomeTrucksRequestItem
import com.delhivery.axle.ui.home.fragments.trucks.HomeTrucksSearchItem
import com.delhivery.axle.ui.home.fragments.trucks.HomeTrucksWarningItem_NoTrucks
import com.delhivery.axle.ui.home.fragments.trucks.HomeTrucksWarningItem_TimeOut
import com.delhivery.axle.utils.extensions.errorResponseBody
import com.delhivery.axle.utils.extensions.errorTPSResponseBody
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.gson.JsonObject
import org.json.JSONObject
import javax.inject.Inject

class PlacementDetailsViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val tpsRepository: TPSRepository, val userPrefs: UserPrefs
): BaseViewModel() {

    lateinit var homePlacementsItemData: HomePlacementsItemData
    var addressLiveData = MutableLiveData<FacilityAddressResponse>()

    var updateVehicleDetails= MutableLiveData<Boolean>()

    fun getFacilityAddress() {
        if(homePlacementsItemData.originCenterCode!=null)
            compositeDisposable += tpsRepository.getFacilityAddress(homePlacementsItemData.originCenterCode!!)
                .onBackground()
                .subscribe { _tRes, error ->
                    if (!error && _tRes != null) {
                        Log.i("Address", _tRes.toString())
                        addressLiveData.postValue(_tRes)
                    }else{ error.handle()
                    }
                }
    }


    fun updateVehicleDetails(updateVehicleDetailsRequest: UpdateVehicleDetailsRequest) {
        compositeDisposable += tpsRepository.updateVehicleDetails(updateVehicleDetailsRequest)
            .onBackground()
            .subscribe { _tRes, error ->
                if (!error && _tRes != null) {

                    updateVehicleDetails.postValue(true)
                }else{
                    updateVehicleDetails.postValue(false)
                    val errorBody = error.errorTPSResponseBody()
                            ?.messageBody
                    if (errorBody != null) {
                            Throwable(errorBody.toString()).handle()
                    } else {
                        error?.handle()
                    }
                }
            }
    }

    fun getAllInventories(searchText:String){

        val jsonObject = JsonObject()
        jsonObject.addProperty("supplier_id", userPrefs.parentId)

        jsonObject.addProperty("offset", 0)
        jsonObject.addProperty("limit", UserTrucksLoadLimit)
        jsonObject.addProperty("vehicle_prefix",searchText)

        compositeDisposable += inventoryRepository.getInventories(jsonObject)
                .onBackground()
                .subscribe{ _res, error ->
                    if(!error && _res != null) {
                        Log.i("inventories", _res.toString())
                        val trucksList :List<HomeTrucksRequestItemData> = _res.trucks

                        mutableListOf<Pair<BaseHomeTrucksRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                            if(trucksList != null && trucksList.isNotEmpty()) {
                                for (trucks in trucksList) {
                                    add(Pair(HomeTrucksRequestItem(trucks), DataRVAdapterOperationType.AddUpdate))
                                }
                            }
                        }.let {
                           // userTrucksData.postValue(it)
                        }
                    }
                }

    }
}