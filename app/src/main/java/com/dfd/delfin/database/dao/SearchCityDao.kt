package com.dfd.delfin.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dfd.delfin.database.entity.SearchCityEntity


@Dao
interface SearchCityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun newSearchEntry(entry: SearchCityEntity): Long

    @Query("SELECT * FROM search_city ORDER BY created_at DESC LIMIT :limit")
    fun latestSearchEntries(limit: Int = 3): LiveData<List<SearchCityEntity>>

    @Query("SELECT * from search_city ORDER BY created_at LIMIT 1")
    fun getCity(): List<SearchCityEntity>

    @Query("SELECT COUNT(*) FROM search_city")
    fun getCount(): Int

    @Delete
    fun deleteSearchEntry(entry: SearchCityEntity)
}