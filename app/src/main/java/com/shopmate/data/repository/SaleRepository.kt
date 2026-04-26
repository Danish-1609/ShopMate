package com.shopmate.data.repository

import androidx.lifecycle.LiveData
import com.shopmate.data.dao.SaleDao
import com.shopmate.data.dao.TopProduct
import com.shopmate.data.entities.Sale
import com.shopmate.data.entities.SaleTransaction
import com.shopmate.models.CategorySales
import com.shopmate.models.DailySales
import com.shopmate.models.SalesSummary

class SaleRepository(private val saleDao: SaleDao) {

    fun getAllSales(): LiveData<List<Sale>> = saleDao.getAllSales()

    suspend fun getAllSalesSync(): List<Sale> = saleDao.getAllSalesSync()

    fun getSalesByDate(date: String): LiveData<List<Sale>> = saleDao.getSalesByDate(date)

    fun getSalesByDateRange(startDate: String, endDate: String): LiveData<List<Sale>> =
        saleDao.getSalesByDateRange(startDate, endDate)

    suspend fun getSalesByDateRangeSync(startDate: String, endDate: String): List<Sale> =
        saleDao.getSalesByDateRangeSync(startDate, endDate)

    fun getDailySummary(date: String): LiveData<SalesSummary> = saleDao.getDailySummary(date)

    suspend fun getSummaryForRange(startDate: String, endDate: String): SalesSummary =
        saleDao.getSummaryForRange(startDate, endDate)

    suspend fun getSalesByCategory(startDate: String, endDate: String): List<CategorySales> =
        saleDao.getSalesByCategory(startDate, endDate)

    suspend fun getDailySalesChart(startDate: String, endDate: String): List<DailySales> =
        saleDao.getDailySalesChart(startDate, endDate)

    fun getSalesByProduct(productId: Long): LiveData<List<Sale>> =
        saleDao.getSalesByProduct(productId)

    fun getAllTransactions(): LiveData<List<SaleTransaction>> = saleDao.getAllTransactions()

    suspend fun insertSale(sale: Sale): Long = saleDao.insertSale(sale)

    suspend fun insertSales(sales: List<Sale>) = saleDao.insertSales(sales)

    suspend fun insertTransaction(transaction: SaleTransaction): Long =
        saleDao.insertTransaction(transaction)

    fun getTodayRevenue(date: String): LiveData<Double?> = saleDao.getTodayRevenue(date)

    suspend fun getTopSellingProducts(startDate: String): List<TopProduct> =
        saleDao.getTopSellingProducts(startDate)
}
