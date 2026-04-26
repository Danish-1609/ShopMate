package com.shopmate.data.repository

import androidx.lifecycle.LiveData
import com.shopmate.data.dao.ProductDao
import com.shopmate.data.entities.Product
import java.time.LocalDateTime

class ProductRepository(private val productDao: ProductDao) {

    fun getAllProducts(): LiveData<List<Product>> = productDao.getAllProducts()

    suspend fun getAllProductsSync(): List<Product> = productDao.getAllProductsSync()

    fun searchProducts(query: String): LiveData<List<Product>> =
        if (query.isBlank()) productDao.getAllProducts()
        else productDao.searchProducts(query.trim())

    fun getProductsByCategory(category: String): LiveData<List<Product>> =
        productDao.getProductsByCategory(category)

    fun getLowStockProducts(): LiveData<List<Product>> = productDao.getLowStockProducts()

    suspend fun getLowStockProductsSync(): List<Product> = productDao.getLowStockProductsSync()

    fun getTotalProductCount(): LiveData<Int> = productDao.getTotalProductCount()

    fun getLowStockCount(): LiveData<Int> = productDao.getLowStockCount()

    fun getAllCategories(): LiveData<List<String>> = productDao.getAllCategories()

    suspend fun getAllCategoriesSync(): List<String> = productDao.getAllCategoriesSync()

    suspend fun getProductById(id: Long): Product? = productDao.getProductById(id)

    fun getProductByIdLive(id: Long) = productDao.getProductByIdLive(id)

    suspend fun getProductByBarcode(barcode: String): Product? =
        productDao.getProductByBarcode(barcode)

    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)

    suspend fun updateProduct(product: Product) {
        val updatedProduct = product.copy(updatedAt = LocalDateTime.now().toString())
        productDao.updateProduct(updatedProduct)
    }

    suspend fun deleteProduct(product: Product) = productDao.softDeleteProduct(product.id)

    suspend fun getInventoryValue(): Double =
        getAllProductsSync().sumOf { it.costPrice * it.quantity }

    suspend fun decreaseStock(productId: Long, amount: Int) {
        productDao.decreaseStock(productId, amount, LocalDateTime.now().toString())
    }

    suspend fun increaseStock(productId: Long, amount: Int) {
        productDao.increaseStock(productId, amount, LocalDateTime.now().toString())
    }

    fun getProductsSortedByPriceAsc() = productDao.getProductsSortedByPriceAsc()
    fun getProductsSortedByPriceDesc() = productDao.getProductsSortedByPriceDesc()
    fun getProductsSortedByStock() = productDao.getProductsSortedByStock()

    suspend fun getTotalInventoryValue(): Double {
        return getAllProductsSync().sumOf { it.costPrice * it.quantity }
    }

    suspend fun insertSampleProducts() {
        val sampleProducts = listOf(
            Product(name = "Basmati Rice (5kg)", category = "Grains & Cereals", quantity = 50,
                costPrice = 320.0, sellingPrice = 400.0, minStockThreshold = 10, unit = "bag",
                description = "Premium basmati rice, 5kg pack"),
            Product(name = "Tata Salt (1kg)", category = "Condiments", quantity = 30,
                costPrice = 18.0, sellingPrice = 25.0, minStockThreshold = 10, unit = "pcs"),
            Product(name = "Amul Butter (500g)", category = "Dairy", quantity = 15,
                costPrice = 220.0, sellingPrice = 270.0, minStockThreshold = 5, unit = "pcs"),
            Product(name = "Sunflower Oil (1L)", category = "Oils & Fats", quantity = 25,
                costPrice = 130.0, sellingPrice = 160.0, minStockThreshold = 8, unit = "bottle"),
            Product(name = "Lays Classic (50g)", category = "Snacks", quantity = 60,
                costPrice = 15.0, sellingPrice = 20.0, minStockThreshold = 20, unit = "pcs"),
            Product(name = "Coca-Cola 500ml", category = "Beverages", quantity = 3,
                costPrice = 28.0, sellingPrice = 40.0, minStockThreshold = 12, unit = "bottle"),
            Product(name = "Surf Excel (1kg)", category = "Household", quantity = 20,
                costPrice = 110.0, sellingPrice = 145.0, minStockThreshold = 6, unit = "pcs"),
            Product(name = "Maggi Noodles (12pk)", category = "Instant Food", quantity = 0,
                costPrice = 144.0, sellingPrice = 180.0, minStockThreshold = 5, unit = "pack")
        )
        sampleProducts.forEach { productDao.insertProduct(it) }
    }
}
