package com.delhivery.axle.ui.home.fragments.placements
import android.util.Log

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.TPSRepository
import com.delhivery.axle.data.Quadruple
import com.delhivery.axle.data.home.placements.HomePlacementNoDelayItemData
import com.delhivery.axle.data.home.placements.HomePlacementsDurationItemData
import com.delhivery.axle.data.home.placements.HomePlacementsItemData
import com.delhivery.axle.data.home.placements.HomePlacementsTypeItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.utils.DateUtils.getTimeDiff
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import javax.inject.Inject

class HomePlacementsViewModel @Inject constructor(
        private val tpsRepository: TPSRepository
) : BaseViewModel(){


        var userLoadsData =
        MutableLiveData<List<Pair<BaseHomePlacementsRVAdapterItem<*>, DataRVAdapterOperationType>>>()
        var userLoadsDataFetch =
        MutableLiveData<List<Pair<BaseHomePlacementsRVAdapterItem<*>, DataRVAdapterOperationType>>>()

        /* data loading live data */
        var dataLoadingLiveData = MutableLiveData<Boolean>()

        var missingDataLiveData = MutableLiveData<Pair<Quadruple<Int,Int,Int,Int>,Int>>()
        var totalPlacementLiveData = MutableLiveData<Triple<Int,Int,Int>>()

        private var expectedPlacementList = ArrayList<HomePlacementsItemData>()
        private var delayedPlacementList = ArrayList<HomePlacementsItemData>()
        private var missingDetailsDelayedPlacementList = ArrayList<HomePlacementsItemData>()
        private var missingDetailsExpectedPlacementList = ArrayList<HomePlacementsItemData>()

        private var _0_2hoursPlacementList = ArrayList<HomePlacementsItemData>()
        private var _2_4hoursPlacementList = ArrayList<HomePlacementsItemData>()
        private var _4_6hoursPlacementList = ArrayList<HomePlacementsItemData>()
        private var _6_12hoursPlacementList = ArrayList<HomePlacementsItemData>()
        private var _12_18hoursPlacementList = ArrayList<HomePlacementsItemData>()

        fun fetchPlacementLoads(placementType:String) {
        Pair(HomePlacementsProgressItem(), DataRVAdapterOperationType.AddUpdate).let { userLoadsData.postValue(listOf(it)) }
        dataLoadingLiveData.postValue(true)

        compositeDisposable += tpsRepository.fetchPlacementTransactions()
        .onBackground()
        .subscribe { _tRes, error ->
        if (!error && _tRes != null) {
                expectedPlacementList.clear()
                delayedPlacementList.clear()
                missingDetailsDelayedPlacementList.clear()
                missingDetailsExpectedPlacementList.clear()
                _0_2hoursPlacementList.clear()
                _2_4hoursPlacementList.clear()
                _4_6hoursPlacementList.clear()
                _6_12hoursPlacementList.clear()
                _12_18hoursPlacementList.clear()


                for (loads in _tRes.ftlAdhoc ){
                        loads.loadType= LoadTypes.ftlAdhoc.name
                        segregateLoadType(loads)
                }
                for (loads in _tRes.ftlRegular ){
                        loads.loadType= LoadTypes.ftlRegular.name
                        segregateLoadType(loads)
                }
                for (loads in _tRes.intracityAdhoc ){
                        loads.loadType= LoadTypes.intracityAdhoc.name
                        segregateLoadType(loads)
                }
                for (loads in _tRes.intracityRegular ){
                        loads.loadType= LoadTypes.intracityRegular.name
                        segregateLoadType(loads)
                }
                for (loads in _tRes.orionFixed ){
                        loads.loadType= LoadTypes.orionFixed.name
                        segregateLoadType(loads)
                }
                for (loads in _tRes.orionSpot ){
                        loads.loadType= LoadTypes.orionSpot.name
                        segregateLoadType(loads)
                }
//                var ftlAdhocMissingCount =0
//                var ftlContractMissingCount =0
//                var intracityAdhocMissingCount =0
//                var intracityContractMissingCount =0
//                for(load in missingDetailsExpectedPlacementList){
//                        when(load.loadType){
//                                LoadTypes.ftlAdhoc.name->  {
//                                        ftlAdhocMissingCount++ }
//                                LoadTypes.ftlRegular.name-> {
//                                        ftlContractMissingCount++ }
//                                LoadTypes.intracityRegular.name->  {
//                                        intracityContractMissingCount++ }
//                                LoadTypes.intracityAdhoc.name->  {
//                                        intracityAdhocMissingCount++ }
//
//                        }
//                }
//                for (load in missingDetailsDelayedPlacementList){
//                        when(load.loadType){
//                                LoadTypes.ftlAdhoc.name->
//                                        ftlAdhocMissingCount++
//                                LoadTypes.ftlRegular.name->
//                                        ftlContractMissingCount++
//                                LoadTypes.intracityRegular.name->
//                                        intracityContractMissingCount++
//                                LoadTypes.intracityAdhoc.name->
//                                        intracityAdhocMissingCount++
//                        }
//                }
//                missingDataLiveData.postValue(Pair(Quadruple(ftlAdhocMissingCount,ftlContractMissingCount,intracityAdhocMissingCount,intracityContractMissingCount),missingDetailsDelayedPlacementList.size+missingDetailsExpectedPlacementList.size))
                Log.d("HomePlacementsViewModel", "Posting totalPlacementLiveData - Delayed: ${delayedPlacementList.size}, Expected: ${expectedPlacementList.size}")
                totalPlacementLiveData.postValue(Triple(delayedPlacementList.size,missingDetailsDelayedPlacementList.size+missingDetailsExpectedPlacementList.size,expectedPlacementList.size))
                mutableListOf<Pair<BaseHomePlacementsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                        /* remove progress item */
                        add( Pair(HomePlacementsProgressItem(), DataRVAdapterOperationType.Remove))
                        when (placementType) {
                            PlacementTypes.Delayed.name -> {
//                                    for(load in expectedPlacementList)
//                                            segregateBasedOnTimeInterval(load)
                                    if(delayedPlacementList.size==0){
                                            add( Pair(HomePlacementsNoDelayItem(
                                                    HomePlacementNoDelayItemData("Whohoo! No delays in placements!","No_Delay")
                                            ), DataRVAdapterOperationType.AddUpdate))
                                    }
                                    for (load in delayedPlacementList){
                                            add(Pair(HomeVehiclePlacementsRequestItem(load), DataRVAdapterOperationType.Add))

                            }
//                                    for(item in addItemBasisLoadTypeAndDuration(PlacementTypes.Delayed.name)){
//                                          add(item)
//                                    }
                            }
//                            PlacementTypes.MissingDetails.name -> {
//                                    add(Pair(HomePlacementsTypeItem(HomePlacementsTypeItemData("Details Missing")), DataRVAdapterOperationType.Add))
//
//                                    for(load in missingDetailsExpectedPlacementList){
//                                            segregateBasedOnTimeInterval(load)
//                                    }
//                                    if(missingDetailsDelayedPlacementList.size==0){
//                                            add( Pair(HomePlacementsNoDelayItem(
//                                                    HomePlacementNoDelayItemData("Whohoo! No delays in placements!","No_Delay")
//                                            ), DataRVAdapterOperationType.AddUpdate))
//                                    }
//
//                                    for (load in missingDetailsDelayedPlacementList){
//                                            add(Pair(HomeVehiclePlacementsRequestItem(load), DataRVAdapterOperationType.Add))}
//
//                                    for(item in addItemBasisLoadTypeAndDuration(PlacementTypes.MissingDetails.name)){
//                                            add(item)
//                                    }
//                            }
                            PlacementTypes.Expected.name -> {
                                  //  add(Pair(HomePlacementsTypeItem(HomePlacementsTypeItemData("Expected")), DataRVAdapterOperationType.Add))
                                    for(load in expectedPlacementList)
                                            segregateBasedOnTimeInterval(load)
                                    for(item in addItemBasisLoadTypeAndDuration(PlacementTypes.Expected.name)){
                                            add(item)
                                    }
                            }
                        }
                }.let { userLoadsData.postValue(it) }
        }else{
                mutableListOf<Pair<BaseHomePlacementsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                        /* remove progress item */
                        add(Pair(HomePlacementsProgressItem(), DataRVAdapterOperationType.Remove))
                        /* add api time out item */
                        add(Pair(
                                HomePlacementsWarningItem_TimeOut,
                                DataRVAdapterOperationType.AddUpdate
                        ))
                }
                        .let {
                                userLoadsData.postValue(it) }
                        }
                dataLoadingLiveData.postValue(false)
                }
        }


        private fun segregateLoadType(load:HomePlacementsItemData){

                if(load.status=="Expected" || load.status=="Marked-in"){
                        if((load.vehicleNumber==null || load.driverName==null|| load.driverPhone==null )){
                                missingDetailsExpectedPlacementList.add(load)
                        }
                                expectedPlacementList.add(load)

                }
                if (load.status=="Delayed"){
                        if((load.vehicleNumber==null || load.driverName==null|| load.driverPhone==null)){
                                missingDetailsDelayedPlacementList.add(load)
                        }
                                delayedPlacementList.add(load)

                }
        }

        private fun segregateBasedOnTimeInterval(load:HomePlacementsItemData){
                if(load.reportingTime!=null)
                        when(getTimeDiff(load.reportingTime)){
                                "0-2" -> _0_2hoursPlacementList.add(load)
                                "2-4" -> _2_4hoursPlacementList.add(load)
                                "4-6" -> _4_6hoursPlacementList.add(load)
                                "6-12" -> _6_12hoursPlacementList.add(load)
                                "12-18" -> _12_18hoursPlacementList.add(load)
                                else ->{}
                        }

        }

        private fun addItemBasisLoadTypeAndDuration(placementType: String):MutableList<Pair<BaseHomePlacementsRVAdapterItem<*>, DataRVAdapterOperationType>>{
                return mutableListOf<Pair<BaseHomePlacementsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                add(Pair(HomePlacementsDurationItem(HomePlacementsDurationItemData("Expected in 0–2 hrs")), DataRVAdapterOperationType.Add))
                 var title = ""
                 var status = ""
                 when(placementType){
                         PlacementTypes.Delayed.name->{ title = "No upcoming vehicles in the pipeline."
                         status = "No_Delay"}
                         PlacementTypes.MissingDetails.name-> {title = "Awesome! No placements with missing details"
                                 status = "Missing"}
                         PlacementTypes.Expected.name-> {title = "No upcoming vehicles in the pipeline."
                                 status = "No_Pipeline"}
                 }
                for(load in _0_2hoursPlacementList){
                                add(Pair(HomeVehiclePlacementsRequestItem(load), DataRVAdapterOperationType.Add))
                        }
                if (_0_2hoursPlacementList.size==0)add(Pair(HomePlacementsNoDelayItem(HomePlacementNoDelayItemData(title,status)),DataRVAdapterOperationType.Add))
                add(Pair(HomePlacementsDurationItem(HomePlacementsDurationItemData("Expected in 2–4 hrs")), DataRVAdapterOperationType.Add))
                for(load in _2_4hoursPlacementList){
                        add(Pair(HomeVehiclePlacementsRequestItem(load), DataRVAdapterOperationType.Add))

                }
                if (_2_4hoursPlacementList.size==0)add(Pair(HomePlacementsNoDelayItem( HomePlacementNoDelayItemData(title,status)),DataRVAdapterOperationType.Add))

                add(Pair(HomePlacementsDurationItem(HomePlacementsDurationItemData("Expected in 4–6 hrs")), DataRVAdapterOperationType.Add))
                for(load in _4_6hoursPlacementList){
                        add(Pair(HomeVehiclePlacementsRequestItem(load), DataRVAdapterOperationType.Add))
                }
                if (_4_6hoursPlacementList.size==0)add(Pair(HomePlacementsNoDelayItem( HomePlacementNoDelayItemData(title,status)),DataRVAdapterOperationType.Add))

                add(Pair(HomePlacementsDurationItem(HomePlacementsDurationItemData("Expected in 6–12 hrs")), DataRVAdapterOperationType.Add))
                for(load in _6_12hoursPlacementList){
                        add(Pair(HomeVehiclePlacementsRequestItem(load), DataRVAdapterOperationType.Add))
                }
                if (_6_12hoursPlacementList.size==0)add(Pair(HomePlacementsNoDelayItem( HomePlacementNoDelayItemData(title,status)),DataRVAdapterOperationType.Add))

                add(Pair(HomePlacementsDurationItem(HomePlacementsDurationItemData("Expected in 12–18 hrs")), DataRVAdapterOperationType.Add))
                for(load in _12_18hoursPlacementList){
                        add(Pair(HomeVehiclePlacementsRequestItem(load), DataRVAdapterOperationType.Add))
                }
                if (_12_18hoursPlacementList.size==0)add(Pair(HomePlacementsNoDelayItem( HomePlacementNoDelayItemData(title,status)),DataRVAdapterOperationType.Add))

        }

        }
}
