package com.shopmate.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "barcode")
    val barcode: String? = null,

    @ColumnInfo(name = "quantity")
    val quantity: Int,

    @ColumnInfo(name = "cost_price")
    val costPrice: Double,

    @ColumnInfo(name = "selling_price")
    val sellingPrice: Double,

    @ColumnInfo(name = "min_stock_threshold")
    val minStockThreshold: Int,

    @ColumnInfo(name = "unit")
    val unit: String = "pcs",

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: String = LocalDateTime.now().toString(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: String = LocalDateTime.now().toString(),

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
) {
    val isLowStock: Boolean
        get() = quantity <= minStockThreshold

    val isOutOfStock: Boolean
        get() = quantity == 0

    val profitMargin: Double
        get() = if (costPrice > 0) ((sellingPrice - costPrice) / costPrice) * 100 else 0.0

    val stockStatus: StockStatus
        get() = when {
            quantity == 0 -> StockStatus.OUT_OF_STOCK
            quantity <= minStockThreshold -> StockStatus.LOW_STOCK
            else -> StockStatus.IN_STOCK
        }
}

enum class StockStatus {
    IN_STOCK, LOW_STOCK, OUT_OF_STOCK
}
