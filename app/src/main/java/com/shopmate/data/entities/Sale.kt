package com.shopmate.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("product_id")]
)
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "product_id")
    val productId: Long?,

    @ColumnInfo(name = "product_name")
    val productName: String, // Snapshot at time of sale

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "quantity_sold")
    val quantitySold: Int,

    @ColumnInfo(name = "selling_price")
    val sellingPrice: Double,

    @ColumnInfo(name = "cost_price")
    val costPrice: Double,

    @ColumnInfo(name = "total_amount")
    val totalAmount: Double,

    @ColumnInfo(name = "profit")
    val profit: Double,

    @ColumnInfo(name = "discount")
    val discount: Double = 0.0,

    @ColumnInfo(name = "payment_method")
    val paymentMethod: String = "Cash",

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "sale_date")
    val saleDate: String = LocalDateTime.now().toString(),

    @ColumnInfo(name = "transaction_id")
    val transactionId: String = java.util.UUID.randomUUID().toString()
)

@Entity(tableName = "sale_transactions")
data class SaleTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "transaction_id")
    val transactionId: String,

    @ColumnInfo(name = "total_amount")
    val totalAmount: Double,

    @ColumnInfo(name = "total_profit")
    val totalProfit: Double,

    @ColumnInfo(name = "discount")
    val discount: Double = 0.0,

    @ColumnInfo(name = "payment_method")
    val paymentMethod: String = "Cash",

    @ColumnInfo(name = "customer_name")
    val customerName: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: String = LocalDateTime.now().toString()
)
