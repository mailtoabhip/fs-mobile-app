package com.dfd.delfin.ui.profile.raterewards.fragments.rewards

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.TruckRepository
import com.dfd.delfin.api.repository.UserRepository
import com.dfd.delfin.data.yourrewards.RangeCondition
import com.dfd.delfin.data.yourrewards.YourRewardsItemData
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.UserPrefs
import com.google.gson.JsonObject
import javax.inject.Inject
import com.google.gson.Gson




class YourRewardsFragmentViewModel   @Inject constructor(private val truckRepository: TruckRepository,private val userRepository: UserRepository,
  val userPrefs: UserPrefs
): BaseViewModel() {

  var userRewardsData =
    MutableLiveData<List<Pair<BaseYourRewardsRVAdapterItem<*>, DataRVAdapterOperationType>>>()
  var dataLoadingLiveData = MutableLiveData<Boolean>()
  var hasMoreData = true
  var offset = 0
  var total = 0
  var paginateCount = 0
  var startDate = ""
  var endDate = ""
  fun fetchSupplierRewards(paginate: Boolean = false) {
    if (!paginate) {
      offset = 0
    } else if (paginate && !hasMoreData) {
      return
    }

    if (paginate) {
      paginateCount += 1
      Pair(
        YourRewardsProgressItem(), DataRVAdapterOperationType.AddUpdate
      ).let { userRewardsData.postValue(listOf(it)) }
    }
    val jsonObject = JsonObject()
    jsonObject.addProperty("sp_id", userPrefs.userId())
    jsonObject.addProperty("offset", offset)
    jsonObject.addProperty("limit", 10)
    var rangeFilters=  ArrayList<RangeCondition>()
    val rangeConditionStart = RangeCondition("submitted_date","gte",startDate+"T00:00:00")
    rangeFilters.add(rangeConditionStart)
    val rangeConditionEnd = RangeCondition("submitted_date","lte",endDate+"T23:59:59")
    rangeFilters.add(rangeConditionEnd)
    jsonObject.add("range_filters", Gson().toJsonTree(rangeFilters))

    dataLoadingLiveData.postValue(true)

    compositeDisposable += truckRepository.getSupplierRewards(jsonObject)
      .onBackground()
      .progress()
      .subscribe { _res, error ->
        if (!error && _res != null) {
          offset += _res.rewardsDetails.size
          total = _res.total
          hasMoreData = _res.hasNext
          val rewardsList: MutableList<YourRewardsItemData> = mutableListOf()
          for((i,item) in _res.rewardsDetails.withIndex()){
            rewardsList.add(item)
          }
          mutableListOf<Pair<BaseYourRewardsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
            add(Pair(YourRewardsProgressItem(), DataRVAdapterOperationType.Remove))

            if (rewardsList.isNotEmpty()) {
              for (rewards in rewardsList) {
                add(Pair(YourRewardsItem(rewards), DataRVAdapterOperationType.Add))
              }

            } else {
              add(Pair(YourRewardsWarningItem_NoRewards, DataRVAdapterOperationType.AddUpdate))
            }
          }
            .let {
              userRewardsData.postValue(it)
            }
        } else {
          mutableListOf<Pair<BaseYourRewardsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
            add(Pair(YourRewardsProgressItem(), DataRVAdapterOperationType.Remove))
            add(Pair(YourRewardsWarningItem_TimeOut, DataRVAdapterOperationType.AddUpdate))
          }
            .let { userRewardsData.postValue(it) }
        }

        dataLoadingLiveData.postValue(false)
      }
  }
}