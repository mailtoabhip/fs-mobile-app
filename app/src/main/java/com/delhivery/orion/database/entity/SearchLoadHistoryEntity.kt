package com.delhivery.orion.database.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "search_load_history")
data class SearchLoadHistoryEntity(
  @ColumnInfo(name = "origin") var originCity: String,
  @ColumnInfo(name = "dest") var destinationCity: String,
  @ColumnInfo(name = "type") var truckType: String,
  @ColumnInfo(name = "size") var truckSize: String,
  @ColumnInfo(name = "created_at") var createdAt: Long = System.currentTimeMillis(),
  @PrimaryKey(autoGenerate = true) var id: Long? = null
)