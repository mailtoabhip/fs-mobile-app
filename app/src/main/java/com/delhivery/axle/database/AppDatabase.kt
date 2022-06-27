package com.delhivery.axle.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.delhivery.axle.database.dao.SearchCityDao
import com.delhivery.axle.database.dao.SearchHistoryDao
import com.delhivery.axle.database.dao.UserDao
import com.delhivery.axle.database.entity.SearchCityEntity
import com.delhivery.axle.database.entity.SearchLoadHistoryEntity
import com.delhivery.axle.database.entity.User

/**
 * App Database, place all entities and DAO's here,
 * also update version, incase any updates to entities
 *
 */
@Database(
    entities = [User::class, SearchLoadHistoryEntity::class, SearchCityEntity::class], version = 7, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun userDao(): UserDao

  abstract fun searchHistoryDao(): SearchHistoryDao

  abstract fun searchCityDao(): SearchCityDao
}