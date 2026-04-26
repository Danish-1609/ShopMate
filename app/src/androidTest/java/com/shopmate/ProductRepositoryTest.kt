package com.shopmate

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shopmate.data.dao.ProductDao
import com.shopmate.data.database.ShopDatabase
import com.shopmate.data.entities.Product
import com.shopmate.data.repository.ProductRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductRepositoryTest {

    private lateinit var db: ShopDatabase
    private lateinit var dao: ProductDao
    private lateinit var repo: ProductRepository

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ShopDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.productDao()
        repo = ProductRepository(dao)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAndRetrieveProduct() = runBlocking {
        val product = Product(
            name = "Test Rice",
            category = "Grains",
            quantity = 50,
            costPrice = 100.0,
            sellingPrice = 130.0,
            minStockThreshold = 10
        )
        val id = repo.insertProduct(product)
        assertTrue(id > 0)

        val retrieved = repo.getProductById(id)
        assertNotNull(retrieved)
        assertEquals("Test Rice", retrieved!!.name)
        assertEquals(50, retrieved.quantity)
    }

    @Test
    fun lowStockDetection() = runBlocking {
        val okProduct = Product(
            name = "OK Stock", category = "Test",
            quantity = 20, costPrice = 10.0, sellingPrice = 15.0,
            minStockThreshold = 5
        )
        val lowProduct = Product(
            name = "Low Stock", category = "Test",
            quantity = 3, costPrice = 10.0, sellingPrice = 15.0,
            minStockThreshold = 5
        )
        repo.insertProduct(okProduct)
        repo.insertProduct(lowProduct)

        val lowItems = repo.getLowStockProductsSync()
        assertEquals(1, lowItems.size)
        assertEquals("Low Stock", lowItems[0].name)
        assertTrue(lowItems[0].isLowStock)
    }

    @Test
    fun decreaseStockUpdatesQuantity() = runBlocking {
        val product = Product(
            name = "Stock Test", category = "Test",
            quantity = 20, costPrice = 10.0, sellingPrice = 15.0,
            minStockThreshold = 5
        )
        val id = repo.insertProduct(product)
        repo.decreaseStock(id, 7)

        val updated = repo.getProductById(id)
        assertEquals(13, updated!!.quantity)
    }

    @Test
    fun softDeletePreservesRecord() = runBlocking {
        val product = Product(
            name = "Delete Me", category = "Test",
            quantity = 5, costPrice = 10.0, sellingPrice = 15.0,
            minStockThreshold = 1
        )
        val id = repo.insertProduct(product)
        val inserted = repo.getProductById(id)
        repo.deleteProduct(inserted!!)

        // After soft delete, getProductById should return the row with is_active=false
        val afterDelete = dao.getProductById(id)
        assertNotNull(afterDelete)
        assertEquals(false, afterDelete!!.isActive)

        // But getAllProductsSync should not include it
        val allActive = repo.getAllProductsSync()
        assertTrue(allActive.none { it.id == id })
    }

    @Test
    fun profitMarginCalculation() {
        val product = Product(
            name = "Margin Test", category = "Test",
            quantity = 10, costPrice = 100.0, sellingPrice = 150.0,
            minStockThreshold = 2
        )
        assertEquals(50.0, product.profitMargin, 0.01)
    }

    @Test
    fun stockStatusEnum() {
        val outOfStock = Product(
            name = "OOS", category = "T",
            quantity = 0, costPrice = 10.0, sellingPrice = 15.0,
            minStockThreshold = 5
        )
        val lowStock = Product(
            name = "Low", category = "T",
            quantity = 3, costPrice = 10.0, sellingPrice = 15.0,
            minStockThreshold = 5
        )
        val inStock = Product(
            name = "Good", category = "T",
            quantity = 20, costPrice = 10.0, sellingPrice = 15.0,
            minStockThreshold = 5
        )

        assertEquals(com.shopmate.data.entities.StockStatus.OUT_OF_STOCK, outOfStock.stockStatus)
        assertEquals(com.shopmate.data.entities.StockStatus.LOW_STOCK, lowStock.stockStatus)
        assertEquals(com.shopmate.data.entities.StockStatus.IN_STOCK, inStock.stockStatus)
    }
}
