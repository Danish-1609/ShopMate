package com.shopmate.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.shopmate.R
import com.shopmate.ShopMateApp
import com.shopmate.data.entities.Product
import com.shopmate.ui.MainActivity

object NotificationHelper {

    fun sendLowStockNotification(context: Context, products: List<Product>) {
        if (products.isEmpty()) return

        val notificationManager = context.getSystemService(NotificationManager::class.java)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "inventory")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (products.size == 1)
            "Low Stock: ${products[0].name}"
        else
            "${products.size} items running low on stock"

        val message = if (products.size == 1)
            "Only ${products[0].quantity} ${products[0].unit} remaining (min: ${products[0].minStockThreshold})"
        else
            products.take(3).joinToString(", ") { it.name } + if (products.size > 3) "..." else ""

        val notification = NotificationCompat.Builder(context, ShopMateApp.CHANNEL_LOW_STOCK)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(products.joinToString("\n") { 
                    "• ${it.name}: ${it.quantity} ${it.unit} left"
                })
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(ShopMateApp.NOTIFICATION_LOW_STOCK_ID, notification)
    }

    fun sendDailySummaryNotification(
        context: Context,
        revenue: Double,
        transactions: Int
    ) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "reports")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ShopMateApp.CHANNEL_DAILY_SUMMARY)
            .setSmallIcon(R.drawable.ic_reports)
            .setContentTitle("Today's Sales Summary")
            .setContentText("Revenue: ₹${String.format("%.2f", revenue)} | Transactions: $transactions")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(ShopMateApp.NOTIFICATION_SUMMARY_ID, notification)
    }
}
