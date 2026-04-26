package com.shopmate.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.shopmate.data.entities.Sale
import com.shopmate.data.entities.SaleTransaction
import com.shopmate.models.SalesSummary
import com.shopmate.models.CategorySales
import com.shopmate.models.DailySales

@Dao
interface SaleDao {

    @Query("SELECT * FROM sales ORDER BY sale_date DESC")
    fun getAllSales(): LiveData<List<Sale>>

    @Query("SELECT * FROM sales ORDER BY sale_date DESC")
    suspend fun getAllSalesSync(): List<Sale>

    @Query("SELECT * FROM sales WHERE sale_date LIKE :date || '%' ORDER BY sale_date DESC")
    fun getSalesByDate(date: String): LiveData<List<Sale>>

    @Query("SELECT * FROM sales WHERE sale_date >= :startDate AND sale_date <= :endDate ORDER BY sale_date DESC")
    fun getSalesByDateRange(startDate: String, endDate: String): LiveData<List<Sale>>

    @Query("SELECT * FROM sales WHERE sale_date >= :startDate AND sale_date <= :endDate ORDER BY sale_date DESC")
    suspend fun getSalesByDateRangeSync(startDate: String, endDate: String): List<Sale>

    @Query("""
        SELECT 
            COALESCE(SUM(total_amount), 0) as totalRevenue,
            COALESCE(SUM(profit), 0) as totalProfit,
            COALESCE(SUM(quantity_sold), 0) as totalQuantity,
            COUNT(*) as transactionCount
        FROM sales WHERE sale_date LIKE :date || '%'
    """)
    fun getDailySummary(date: String): LiveData<SalesSummary>

    @Query("""
        SELECT 
            COALESCE(SUM(total_amount), 0) as totalRevenue,
            COALESCE(SUM(profit), 0) as totalProfit,
            COALESCE(SUM(quantity_sold), 0) as totalQuantity,
            COUNT(*) as transactionCount
        FROM sales WHERE sale_date >= :startDate AND sale_date <= :endDate
    """)
    suspend fun getSummaryForRange(startDate: String, endDate: String): SalesSummary

    @Query("""
        SELECT category, 
               SUM(total_amount) as totalAmount, 
               SUM(quantity_sold) as totalQuantity,
               COUNT(*) as transactionCount
        FROM sales 
        WHERE sale_date >= :startDate AND sale_date <= :endDate
        GROUP BY category 
        ORDER BY totalAmount DESC
    """)
    suspend fun getSalesByCategory(startDate: String, endDate: String): List<CategorySales>

    @Query("""
        SELECT substr(sale_date, 1, 10) as date, 
               SUM(total_amount) as totalAmount,
               SUM(profit) as totalProfit,
               COUNT(*) as transactionCount
        FROM sales 
        WHERE sale_date >= :startDate AND sale_date <= :endDate
        GROUP BY substr(sale_date, 1, 10)
        ORDER BY date ASC
    """)
    suspend fun getDailySalesChart(startDate: String, endDate: String): List<DailySales>

    @Query("SELECT * FROM sales WHERE product_id = :productId ORDER BY sale_date DESC LIMIT 20")
    fun getSalesByProduct(productId: Long): LiveData<List<Sale>>

    @Query("SELECT * FROM sale_transactions ORDER BY created_at DESC")
    fun getAllTransactions(): LiveData<List<SaleTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSales(sales: List<Sale>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: SaleTransaction): Long

    @Query("SELECT SUM(total_amount) FROM sales WHERE sale_date LIKE :date || '%'")
    fun getTodayRevenue(date: String): LiveData<Double?>

    @Query("SELECT product_name, SUM(quantity_sold) as totalQty, SUM(total_amount) as totalAmount FROM sales WHERE sale_date >= :startDate GROUP BY product_name ORDER BY totalQty DESC LIMIT 10")
    suspend fun getTopSellingProducts(startDate: String): List<TopProduct>
}

data class TopProduct(
    val product_name: String,
    val totalQty: Int,
    val totalAmount: Double
)
