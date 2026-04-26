package com.shopmate.models

import androidx.room.ColumnInfo

data class SalesSummary(
    val totalRevenue: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalQuantity: Int = 0,
    val transactionCount: Int = 0
)

data class CategorySales(
    val category: String,
    val totalAmount: Double,
    val totalQuantity: Int,
    val transactionCount: Int
)

data class DailySales(
    val date: String,
    val totalAmount: Double,
    val totalProfit: Double,
    val transactionCount: Int
)

data class CartItem(
    val productId: Long,
    val productName: String,
    val category: String,
    val sellingPrice: Double,
    val costPrice: Double,
    var quantity: Int,
    val maxQuantity: Int,
    val unit: String = "pcs"
) {
    val totalAmount: Double
        get() = sellingPrice * quantity

    val totalProfit: Double
        get() = (sellingPrice - costPrice) * quantity
}

data class ReportData(
    val period: String,
    val totalRevenue: Double,
    val totalProfit: Double,
    val totalSales: Int,
    val topProducts: List<String>,
    val categoryBreakdown: Map<String, Double>
)

data class DashboardData(
    val totalProducts: Int = 0,
    val lowStockCount: Int = 0,
    val todayRevenue: Double = 0.0,
    val todayTransactions: Int = 0,
    val outOfStockCount: Int = 0,
    val totalInventoryValue: Double = 0.0
)

enum class SortOption {
    NAME_ASC, NAME_DESC, PRICE_ASC, PRICE_DESC, STOCK_ASC, STOCK_DESC
}

enum class FilterOption {
    ALL, LOW_STOCK, OUT_OF_STOCK, IN_STOCK
}

data class ExportResult(
    val success: Boolean,
    val filePath: String = "",
    val message: String = ""
)
