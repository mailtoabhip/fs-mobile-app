package com.delhivery.orion.database.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "user")
data class User(
        @PrimaryKey(autoGenerate = true) var id: Long? = null,
        var name: String,
        var dob: Long
)