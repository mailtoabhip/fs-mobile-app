package com.delhivery.orion.database.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.delhivery.orion.database.entity.User
import io.reactivex.Single

@Dao
interface UserDao {
  @Query("SELECT * FROM user")
  fun getAllUsers(): Single<List<User>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertUser(user: User)
}