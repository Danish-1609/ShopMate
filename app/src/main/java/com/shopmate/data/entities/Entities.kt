package com.shopmate.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "color")
    val color: String = "#6200EE",

    @ColumnInfo(name = "icon")
    val icon: String = "ic_category",

    @ColumnInfo(name = "created_at")
    val createdAt: String = java.time.LocalDateTime.now().toString()
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "password_hash")
    val passwordHash: String,

    @ColumnInfo(name = "pin")
    val pin: String? = null,

    @ColumnInfo(name = "shop_name")
    val shopName: String = "My Shop",

    @ColumnInfo(name = "created_at")
    val createdAt: String = java.time.LocalDateTime.now().toString(),

    @ColumnInfo(name = "last_login")
    val lastLogin: String? = null
)

@Entity(tableName = "restock_alerts")
data class RestockAlert(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "product_id")
    val productId: Long,

    @ColumnInfo(name = "product_name")
    val productName: String,

    @ColumnInfo(name = "current_quantity")
    val currentQuantity: Int,

    @ColumnInfo(name = "threshold")
    val threshold: Int,

    @ColumnInfo(name = "is_acknowledged")
    val isAcknowledged: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: String = java.time.LocalDateTime.now().toString()
)
