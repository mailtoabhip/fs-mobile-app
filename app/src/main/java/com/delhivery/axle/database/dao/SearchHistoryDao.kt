package com.delhivery.axle.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.delhivery.axle.database.entity.SearchLoadHistoryEntity

@Dao
interface SearchHistoryDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun newSearchEntry(entry: SearchLoadHistoryEntity): Long

  @Query("SELECT * FROM search_load_history WHERE request_type =:requestType  ORDER BY created_at DESC LIMIT :limit")
  fun latestLoadSearchEntries(limit: Int = 3, requestType:String = "fixed,spot"): LiveData<List<SearchLoadHistoryEntity>>

  @Query("SELECT * FROM search_load_history  WHERE request_type =:requestType AND contract_type =:contractType ORDER BY created_at DESC LIMIT :limit")
  fun latestContractSearchEntries(limit: Int = 3, requestType:String = "contract", contractType:String): LiveData<List<SearchLoadHistoryEntity>>

  @Delete
  fun deleteSearchEntry(entry: SearchLoadHistoryEntity)
}