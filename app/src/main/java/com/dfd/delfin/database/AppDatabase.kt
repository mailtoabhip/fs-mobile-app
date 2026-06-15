package com.dfd.delfin.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dfd.delfin.database.dao.OffersDao
import com.dfd.delfin.database.dao.SearchCityDao
import com.dfd.delfin.database.dao.SearchHistoryDao
import com.dfd.delfin.database.dao.UserDao
import com.dfd.delfin.database.entity.OffersEntity
import com.dfd.delfin.database.entity.SearchCityEntity
import com.dfd.delfin.database.entity.SearchLoadHistoryEntity
import com.dfd.delfin.database.entity.User

/**
 * App Database, place all entities and DAO's here,
 * also update version, incase any updates to entities
 *
 */
@Database(
    entities = [User::class, SearchLoadHistoryEntity::class, SearchCityEntity::class, OffersEntity::class], version = 10, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun userDao(): UserDao

  abstract fun searchHistoryDao(): SearchHistoryDao

  abstract fun searchCityDao(): SearchCityDao

  abstract fun offersDao(): OffersDao
}