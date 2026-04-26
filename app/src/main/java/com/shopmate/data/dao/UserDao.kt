package com.shopmate.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.shopmate.data.entities.RestockAlert
import com.shopmate.data.entities.User

@Dao
interface UserDao {

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUser(): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET last_login = :time WHERE id = :userId")
    suspend fun updateLastLogin(userId: Long, time: String)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

@Dao
interface RestockAlertDao {

    @Query("SELECT * FROM restock_alerts WHERE is_acknowledged = 0 ORDER BY created_at DESC")
    fun getActiveAlerts(): LiveData<List<RestockAlert>>

    @Query("SELECT COUNT(*) FROM restock_alerts WHERE is_acknowledged = 0")
    fun getActiveAlertCount(): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: RestockAlert): Long

    @Query("UPDATE restock_alerts SET is_acknowledged = 1 WHERE id = :alertId")
    suspend fun acknowledgeAlert(alertId: Long)

    @Query("UPDATE restock_alerts SET is_acknowledged = 1 WHERE product_id = :productId")
    suspend fun acknowledgeAlertsByProduct(productId: Long)

    @Query("DELETE FROM restock_alerts WHERE is_acknowledged = 1")
    suspend fun clearAcknowledgedAlerts()

    @Query("SELECT COUNT(*) FROM restock_alerts WHERE product_id = :productId AND is_acknowledged = 0")
    suspend fun getActiveAlertCountForProduct(productId: Long): Int
}
