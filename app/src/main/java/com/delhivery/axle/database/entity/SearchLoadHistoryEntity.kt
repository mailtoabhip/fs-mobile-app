package com.delhivery.axle.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.delhivery.axle.data.CityModel

@Entity(tableName = "search_load_history")
data class SearchLoadHistoryEntity(
  @Embedded(prefix = "origin_") var originCity: CityModel,
  @Embedded(prefix = "dest_") var destinationCity: CityModel,
  @ColumnInfo(name = "type") var truckType: String?,
  @ColumnInfo(name = "display_name") var truckDisplayName: String?,
  @ColumnInfo(name = "status") var status: String?,
  @ColumnInfo(name = "request_type") var requestType: String?,
  @ColumnInfo(name = "contract_type") var contractType: String?,
  @ColumnInfo(name = "created_at") var createdAt: Long = System.currentTimeMillis(),
  @PrimaryKey(autoGenerate = true) var id: Long? = null
)