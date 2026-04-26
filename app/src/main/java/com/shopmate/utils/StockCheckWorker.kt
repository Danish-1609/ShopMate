package com.shopmate.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shopmate.ShopMateApp
import com.shopmate.data.entities.RestockAlert

class StockCheckWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val app = context.applicationContext as ShopMateApp
            if (!app.userRepository.isNotificationsEnabled()) return Result.success()

            val lowStockProducts = app.productRepository.getLowStockProductsSync()

            if (lowStockProducts.isNotEmpty()) {
                NotificationHelper.sendLowStockNotification(context, lowStockProducts)

                lowStockProducts.forEach { product ->
                    val hasAlert = app.alertRepository.getActiveAlertCountForProduct(product.id) > 0
                    if (!hasAlert) {
                        app.alertRepository.insertAlert(
                            RestockAlert(
                                productId = product.id,
                                productName = product.name,
                                currentQuantity = product.quantity,
                                threshold = product.minStockThreshold
                            )
                        )
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
