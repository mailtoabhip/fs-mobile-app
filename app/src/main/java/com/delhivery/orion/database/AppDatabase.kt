package com.delhivery.orion.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.delhivery.orion.database.dao.UserDao
import com.delhivery.orion.database.entity.User

/**
 * App Database, place all entities and DAO's here,
 * also update version, incase any updates to entities
 *
 */
@Database(entities = [(User::class)], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}