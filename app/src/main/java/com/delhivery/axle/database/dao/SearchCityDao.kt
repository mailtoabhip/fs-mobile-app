package com.delhivery.axle.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.delhivery.axle.database.entity.SearchCityEntity


@Dao
interface SearchCityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun newSearchEntry(entry: SearchCityEntity): Long

    @Query("SELECT * FROM search_city ORDER BY created_at DESC LIMIT :limit")
    fun latestSearchEntries(limit: Int = 3): LiveData<List<SearchCityEntity>>

    @Delete
    fun deleteSearchEntry(entry: SearchCityEntity)
}