package com.shopmate

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.shopmate.data.database.ShopDatabase
import com.shopmate.data.repository.*

class ShopMateApp : Application() {

    val database by lazy { ShopDatabase.getDatabase(this) }
    val productRepository by lazy { ProductRepository(database.productDao()) }
    val saleRepository by lazy { SaleRepository(database.saleDao()) }
    val userRepository by lazy {
        UserRepository(
            database.userDao(),
            getSharedPreferences("shopmate_prefs", Context.MODE_PRIVATE)
        )
    }
    val alertRepository by lazy { AlertRepository(database.restockAlertDao()) }

    override fun onCreate() {
        super.onCreate()

        // Apply theme
        val prefs = getSharedPreferences("shopmate_prefs", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Low Stock Alert Channel
            val stockChannel = NotificationChannel(
                CHANNEL_LOW_STOCK,
                "Low Stock Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when products are running low"
                enableVibration(true)
            }

            // Daily Summary Channel
            val summaryChannel = NotificationChannel(
                CHANNEL_DAILY_SUMMARY,
                "Daily Summary",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily sales summary notifications"
            }

            notificationManager.createNotificationChannels(listOf(stockChannel, summaryChannel))
        }
    }

    companion object {
        const val CHANNEL_LOW_STOCK = "low_stock_alerts"
        const val CHANNEL_DAILY_SUMMARY = "daily_summary"
        const val NOTIFICATION_LOW_STOCK_ID = 1001
        const val NOTIFICATION_SUMMARY_ID = 1002
    }
}
