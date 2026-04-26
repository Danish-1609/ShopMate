package com.shopmate.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.shopmate.data.entities.Product

@Dao
interface ProductDao {

    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY name ASC")
    fun getAllProducts(): LiveData<List<Product>>

    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY name ASC")
    suspend fun getAllProductsSync(): List<Product>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductByIdLive(id: Long): LiveData<Product?>

    @Query("SELECT * FROM products WHERE barcode = :barcode AND is_active = 1 LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Product?

    @Query("""
        SELECT * FROM products 
        WHERE is_active = 1 
        AND (name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun searchProducts(query: String): LiveData<List<Product>>

    @Query("SELECT * FROM products WHERE is_active = 1 AND category = :category ORDER BY name ASC")
    fun getProductsByCategory(category: String): LiveData<List<Product>>

    @Query("SELECT * FROM products WHERE is_active = 1 AND quantity <= min_stock_threshold ORDER BY quantity ASC")
    fun getLowStockProducts(): LiveData<List<Product>>

    @Query("SELECT * FROM products WHERE is_active = 1 AND quantity <= min_stock_threshold")
    suspend fun getLowStockProductsSync(): List<Product>

    @Query("SELECT COUNT(*) FROM products WHERE is_active = 1")
    fun getTotalProductCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM products WHERE is_active = 1 AND quantity <= min_stock_threshold")
    fun getLowStockCount(): LiveData<Int>

    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY selling_price ASC")
    fun getProductsSortedByPriceAsc(): LiveData<List<Product>>

    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY selling_price DESC")
    fun getProductsSortedByPriceDesc(): LiveData<List<Product>>

    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY quantity ASC")
    fun getProductsSortedByStock(): LiveData<List<Product>>

    @Query("SELECT DISTINCT category FROM products WHERE is_active = 1 ORDER BY category ASC")
    fun getAllCategories(): LiveData<List<String>>

    @Query("SELECT DISTINCT category FROM products WHERE is_active = 1 ORDER BY category ASC")
    suspend fun getAllCategoriesSync(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Query("UPDATE products SET quantity = quantity - :amount, updated_at = :updatedAt WHERE id = :productId")
    suspend fun decreaseStock(productId: Long, amount: Int, updatedAt: String)

    @Query("UPDATE products SET quantity = quantity + :amount, updated_at = :updatedAt WHERE id = :productId")
    suspend fun increaseStock(productId: Long, amount: Int, updatedAt: String)

    @Query("UPDATE products SET is_active = 0 WHERE id = :id")
    suspend fun softDeleteProduct(id: Long)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomProducts(limit: Int): List<Product>
}
