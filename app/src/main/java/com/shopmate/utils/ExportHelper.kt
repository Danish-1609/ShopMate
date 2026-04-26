package com.shopmate.utils

import android.content.Context
import android.os.Environment
import com.google.gson.Gson
import com.shopmate.data.entities.Product
import com.shopmate.data.entities.Sale
import com.shopmate.models.ExportResult
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ExportHelper {

    private val gson = Gson()
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")

    fun exportProductsToCSV(context: Context, products: List<Product>): ExportResult {
        return try {
            val dir = getExportDir(context)
            val timestamp = LocalDateTime.now().format(dateFormatter)
            val file = File(dir, "products_$timestamp.csv")
            FileWriter(file).use { writer ->
                writer.append("ID,Name,Category,Barcode,Quantity,Unit,Cost Price,Selling Price,Min Threshold,Description,Status\n")
                products.forEach { p ->
                    writer.append("${p.id},\"${p.name}\",\"${p.category}\",\"${p.barcode ?: ""}\",${p.quantity},\"${p.unit}\",${p.costPrice},${p.sellingPrice},${p.minStockThreshold},\"${p.description ?: ""}\",${p.stockStatus}\n")
                }
            }
            ExportResult(true, file.absolutePath, "Products exported to ${file.name}")
        } catch (e: Exception) {
            ExportResult(false, "", "Export failed: ${e.message}")
        }
    }

    fun exportSalesToCSV(context: Context, sales: List<Sale>): ExportResult {
        return try {
            val dir = getExportDir(context)
            val timestamp = LocalDateTime.now().format(dateFormatter)
            val file = File(dir, "sales_$timestamp.csv")
            FileWriter(file).use { writer ->
                writer.append("ID,Product Name,Category,Quantity Sold,Selling Price,Cost Price,Total Amount,Profit,Discount,Payment Method,Sale Date\n")
                sales.forEach { s ->
                    writer.append("${s.id},\"${s.productName}\",\"${s.category}\",${s.quantitySold},${s.sellingPrice},${s.costPrice},${s.totalAmount},${s.profit},${s.discount},\"${s.paymentMethod}\",\"${s.saleDate}\"\n")
                }
            }
            ExportResult(true, file.absolutePath, "Sales exported to ${file.name}")
        } catch (e: Exception) {
            ExportResult(false, "", "Export failed: ${e.message}")
        }
    }

    fun backupToJSON(context: Context, products: List<Product>, sales: List<Sale>): ExportResult {
        return try {
            val dir = getExportDir(context)
            val timestamp = LocalDateTime.now().format(dateFormatter)
            val file = File(dir, "shopmate_backup_$timestamp.json")
            val backupData = mapOf(
                "version" to 1,
                "timestamp" to LocalDateTime.now().toString(),
                "products" to products,
                "sales" to sales
            )
            file.writeText(gson.toJson(backupData))
            ExportResult(true, file.absolutePath, "Backup saved to ${file.name}")
        } catch (e: Exception) {
            ExportResult(false, "", "Backup failed: ${e.message}")
        }
    }

    fun restoreFromJSON(context: Context, file: File): RestoreResult {
        return try {
            val json = file.readText()
            val map = gson.fromJson(json, Map::class.java)
            val productsJson = gson.toJson(map["products"])
            val salesJson = gson.toJson(map["sales"])
            val products = gson.fromJson(productsJson, Array<Product>::class.java).toList()
            val sales = gson.fromJson(salesJson, Array<Sale>::class.java).toList()
            RestoreResult(true, products, sales, "Restore successful")
        } catch (e: Exception) {
            RestoreResult(false, emptyList(), emptyList(), "Restore failed: ${e.message}")
        }
    }

    private fun getExportDir(context: Context): File {
        val dir = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "ShopMate")
        } else {
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "ShopMate")
        }
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getExportDirPath(context: Context): String = getExportDir(context).absolutePath
}

data class RestoreResult(
    val success: Boolean,
    val products: List<Product>,
    val sales: List<Sale>,
    val message: String
)
