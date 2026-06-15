package com.dfd.delfin.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dfd.delfin.data.CityModel

@Entity(tableName = "search_city")
data class SearchCityEntity(
    @Embedded(prefix = "origin_") var originCity: CityModel,
    @ColumnInfo(name = "created_at") var createdAt: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) var id: Long? = null
)
