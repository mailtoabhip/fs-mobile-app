package com.delhivery.orion.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.delhivery.orion.database.dao.SearchHistoryDao
import com.delhivery.orion.database.dao.UserDao
import com.delhivery.orion.database.entity.SearchLoadHistoryEntity
import com.delhivery.orion.database.entity.User

/**
 * App Database, place all entities and DAO's here,
 * also update version, incase any updates to entities
 *
 */
@Database(entities = [User::class, SearchLoadHistoryEntity::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
  abstract fun userDao(): UserDao

  abstract fun searchHistoryDao(): SearchHistoryDao
}