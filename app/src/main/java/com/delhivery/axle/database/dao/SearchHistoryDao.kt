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

  @Query("SELECT * FROM search_load_history ORDER BY created_at DESC LIMIT :limit")
  fun latestSearchEntries(limit: Int = 3): LiveData<List<SearchLoadHistoryEntity>>

  @Delete
  fun deleteSearchEntry(entry: SearchLoadHistoryEntity)
}