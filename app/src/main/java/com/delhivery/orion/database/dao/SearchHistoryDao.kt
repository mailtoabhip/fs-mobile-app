package com.delhivery.orion.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.delhivery.orion.database.entity.SearchLoadHistoryEntity

@Dao
interface SearchHistoryDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun newSearchEntry(entry: SearchLoadHistoryEntity): Long

  @Query("SELECT * FROM search_load_history ORDER BY created_at DESC LIMIT :limit")
  fun latestSearchEntries(limit: Int = 3): LiveData<List<SearchLoadHistoryEntity>>

  @Delete
  fun deleteSearchEntry(entry: SearchLoadHistoryEntity)
}