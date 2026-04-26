package com.shopmate.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scheduleStockCheckWorker(context)
        }
    }
}

fun scheduleStockCheckWorker(context: Context) {
    val request = PeriodicWorkRequestBuilder<StockCheckWorker>(3, TimeUnit.HOURS)
        .setConstraints(Constraints.Builder().build())
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "stock_check",
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}
