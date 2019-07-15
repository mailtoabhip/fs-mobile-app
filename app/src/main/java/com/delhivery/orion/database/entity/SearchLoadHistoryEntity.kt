package com.delhivery.orion.database.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.delhivery.orion.data.CityModel

@Entity(tableName = "search_load_history")
data class SearchLoadHistoryEntity(
  @Embedded(prefix = "origin_") var originCity: CityModel,
  @Embedded(prefix = "dest_") var destinationCity: CityModel,
  @ColumnInfo(name = "type") var truckType: String,
  @ColumnInfo(name = "created_at") var createdAt: Long = System.currentTimeMillis(),
  @PrimaryKey(autoGenerate = true) var id: Long? = null
)