package com.delhivery.axle.api.repository

import com.delhivery.axle.api.request.DeactivateTruckRequest
import com.delhivery.axle.api.request.DeleteTruckRequest
import com.delhivery.axle.api.service.CityService
import com.delhivery.axle.api.service.InventoryService
import com.delhivery.axle.data.ClusterResponse
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.utils.extensions.convertMessageResponse
import com.delhivery.axle.utils.extensions.convertResponse
import com.google.gson.JsonObject
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(
    private val inventoryService: InventoryService,
    private val cityService: CityService
): BaseRepository() {

    fun getInventories(request: JsonObject) = inventoryService.getInventories(request).convertResponse()

    fun addInventory(request: JsonObject)= inventoryService.addInventory(request).convertResponse()

    fun activateTruck(request: JsonObject) = inventoryService.activateTruck(request).convertResponse()

    fun editTruck(request: JsonObject) = inventoryService.editTruck(request).convertResponse()

    fun deleteTruck(request: DeleteTruckRequest) = inventoryService.deleteTruck(request).convertMessageResponse()

    fun deActivateTruck(request: DeactivateTruckRequest) = inventoryService.deActivateTruck(request).convertResponse()

    fun getOriginDestinationCluster(originCityId: String, destinationCityId: String): Single<Pair<String, String>> =
        Single.zip(
            cityService.getClusterID(originCityId).convertResponse().subscribeOn(Schedulers.io()),
            cityService.getClusterID(destinationCityId).convertResponse().subscribeOn(Schedulers.io()),
            BiFunction<ClusterResponse, ClusterResponse,
                    Pair<String, String>> { t1, t2 ->
                Pair( if(t1.clusters.isNotEmpty()){
                    t1.clusters[0].clusterId
                }else{
                    "unmapped_cluster"
                },if(t2.clusters.isNotEmpty()){
                    t2.clusters[0].clusterId
                }else{
                    "unmapped_cluster"})

            }
        )

    fun getSingleInventory(request: JsonObject) = inventoryService.getInventory(request).convertResponse()


}

/* User trips pagination load limit */
const val UserTrucksLoadLimit = 10