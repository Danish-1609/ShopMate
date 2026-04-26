package com.shopmate.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.shopmate.data.dao.RestockAlertDao
import com.shopmate.data.dao.UserDao
import com.shopmate.data.entities.RestockAlert
import com.shopmate.data.entities.User
import java.security.MessageDigest

class UserRepository(
    private val userDao: UserDao,
    private val prefs: SharedPreferences
) {
    companion object {
        const val KEY_SKIP_LOGIN = "skip_login"
        const val KEY_LOGGED_IN = "is_logged_in"
        const val KEY_USER_ID = "user_id"
        const val KEY_SHOP_NAME = "shop_name"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_FIRST_LAUNCH = "first_launch"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }

    suspend fun getUser(): User? = userDao.getUser()

    suspend fun getUserByUsername(username: String): User? = userDao.getUserByUsername(username)

    suspend fun createUser(username: String, password: String, shopName: String): Long {
        val user = User(
            username = username,
            passwordHash = hashPassword(password),
            shopName = shopName
        )
        return userDao.insertUser(user)
    }

    suspend fun verifyCredentials(username: String, password: String): User? {
        val user = userDao.getUserByUsername(username) ?: return null
        return if (user.passwordHash == hashPassword(password)) user else null
    }

    suspend fun verifyPin(pin: String): Boolean {
        val user = getUser() ?: return false
        return user.pin == hashPassword(pin)
    }

    suspend fun hasUsers(): Boolean = userDao.getUserCount() > 0

    fun isSkipLogin(): Boolean = prefs.getBoolean(KEY_SKIP_LOGIN, false)
    fun setSkipLogin(skip: Boolean) = prefs.edit().putBoolean(KEY_SKIP_LOGIN, skip).apply()

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)
    fun setLoggedIn(value: Boolean) = prefs.edit().putBoolean(KEY_LOGGED_IN, value).apply()

    fun getShopName(): String = prefs.getString(KEY_SHOP_NAME, "My Shop") ?: "My Shop"
    fun setShopName(name: String) = prefs.edit().putString(KEY_SHOP_NAME, name).apply()

    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, false)
    fun setDarkMode(value: Boolean) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    fun isFirstLaunch(): Boolean = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    fun setFirstLaunch(value: Boolean) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()

    fun isNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    fun setNotificationsEnabled(v: Boolean) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, v).apply()

    fun logout() {
        prefs.edit().putBoolean(KEY_LOGGED_IN, false).apply()
    }

    private fun hashPassword(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

class AlertRepository(private val alertDao: RestockAlertDao) {

    fun getActiveAlerts() = alertDao.getActiveAlerts()

    fun getActiveAlertCount() = alertDao.getActiveAlertCount()

    suspend fun insertAlert(alert: RestockAlert): Long = alertDao.insertAlert(alert)

    suspend fun acknowledgeAlert(alertId: Long) = alertDao.acknowledgeAlert(alertId)

    suspend fun acknowledgeAlertsByProduct(productId: Long) = alertDao.acknowledgeAlertsByProduct(productId)

    suspend fun clearAcknowledgedAlerts() = alertDao.clearAcknowledgedAlerts()

    suspend fun getActiveAlertCountForProduct(productId: Long): Int =
        alertDao.getActiveAlertCountForProduct(productId)
}
