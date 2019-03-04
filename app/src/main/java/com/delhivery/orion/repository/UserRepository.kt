package com.delhivery.orion.repository


import com.delhivery.orion.database.AppDatabase
import com.delhivery.orion.database.entity.User
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User Repository
 * -- contains most of business logic related to User
 *
 */
@Singleton
class UserRepository @Inject constructor(private val appDatabase: AppDatabase) : BaseRepository() {

    fun testRepo() {
        //test function for injection
    }

    /**
     * Add User
     */
    fun addUser(user: User): Single<Boolean> {
        return Single.create<Boolean> {
            try {
                appDatabase.userDao().insertUser(user)
                it.onSuccess(true)
            } catch (e: Exception) {
                it.onError(e)
            }
        }
    }

    /**
     * Get all users from db
     */
    fun getAllUsers(): Single<List<User>> = appDatabase.userDao().getAllUsers()
}