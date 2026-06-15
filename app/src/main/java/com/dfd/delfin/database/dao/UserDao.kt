package com.dfd.delfin.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dfd.delfin.database.entity.User
import io.reactivex.Single

@Dao
interface UserDao {
  @Query("SELECT * FROM user")
  fun getAllUsers(): Single<List<User>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertUser(user: User)
}