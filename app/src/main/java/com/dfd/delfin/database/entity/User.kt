package com.dfd.delfin.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
  @PrimaryKey(autoGenerate = true) var id: Long? = null,
  var name: String,
  var dob: Long
)