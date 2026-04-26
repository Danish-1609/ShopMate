package com.shopmate.viewmodels

import androidx.lifecycle.*
import com.shopmate.data.entities.Product
import com.shopmate.data.entities.RestockAlert
import com.shopmate.data.entities.Sale
import com.shopmate.data.entities.SaleTransaction
import com.shopmate.data.repository.AlertRepository
import com.shopmate.data.repository.ProductRepository
import com.shopmate.data.repository.SaleRepository
import com.shopmate.models.CartItem
import com.shopmate.utils.DateUtils
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class SalesViewModel(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
    private val alertRepository: AlertRepository
) : ViewModel() {

    val allProducts = productRepository.getAllProducts()

    private val _cart = MutableLiveData<MutableList<CartItem>>(mutableListOf())
    val cart: LiveData<MutableList<CartItem>> = _cart

    private val _saleResult = MutableLiveData<SaleResult?>()
    val saleResult: LiveData<SaleResult?> = _saleResult

    private val _todaySales = MutableLiveData<List<Sale>>(emptyList())
    val todaySales: LiveData<List<Sale>> = _todaySales

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _filteredProducts = MediatorLiveData<List<Product>>()
    val filteredProducts: LiveData<List<Product>> = _filteredProducts

    private val _selectedPaymentMethod = MutableLiveData("Cash")
    val selectedPaymentMethod: LiveData<String> = _selectedPaymentMethod

    private val _discount = MutableLiveData(0.0)
    val discount: LiveData<Double> = _discount

    val cartTotal: LiveData<Double> = _cart.map { items ->
        items.sumOf { it.totalAmount } - (_discount.value ?: 0.0)
    }
    val cartProfit: LiveData<Double> = _cart.map { it.sumOf { item -> item.totalProfit } }
    val cartItemCount: LiveData<Int> = _cart.map { it.sumOf { item -> item.quantity } }

    init {
        _filteredProducts.addSource(allProducts) { applyProductSearch(it) }
        _filteredProducts.addSource(_searchQuery) { applyProductSearch(allProducts.value) }
        loadTodaySales()
    }

    private fun applyProductSearch(products: List<Product>?) {
        if (products == null) return
        val q = _searchQuery.value?.trim() ?: ""
        _filteredProducts.value = if (q.isEmpty()) products
        else products.filter {
            it.name.contains(q, true) ||
            it.category.contains(q, true) ||
            it.barcode?.contains(q, true) == true
        }.filter { it.quantity > 0 }
    }

    fun setSearch(query: String) { _searchQuery.value = query }
    fun setPaymentMethod(method: String) { _selectedPaymentMethod.value = method }
    fun setDiscount(amount: Double) {
        _discount.value = amount
        _cart.value = _cart.value // trigger cartTotal recalculation
    }

    fun addToCart(product: Product) {
        val current = _cart.value ?: mutableListOf()
        val existing = current.find { it.productId == product.id }
        if (existing != null) {
            if (existing.quantity < product.quantity) {
                existing.quantity++
                _cart.value = current
            }
        } else {
            current.add(
                CartItem(
                    productId = product.id,
                    productName = product.name,
                    category = product.category,
                    sellingPrice = product.sellingPrice,
                    costPrice = product.costPrice,
                    quantity = 1,
                    maxQuantity = product.quantity,
                    unit = product.unit
                )
            )
            _cart.value = current
        }
    }

    fun removeFromCart(productId: Long) {
        val current = _cart.value ?: return
        current.removeAll { it.productId == productId }
        _cart.value = current
    }

    fun updateCartItemQuantity(productId: Long, qty: Int) {
        val current = _cart.value ?: return
        val item = current.find { it.productId == productId } ?: return
        if (qty <= 0) {
            current.remove(item)
        } else if (qty <= item.maxQuantity) {
            item.quantity = qty
        }
        _cart.value = current
    }

    fun clearCart() {
        _cart.value = mutableListOf()
        _discount.value = 0.0
    }

    fun completeSale(customerName: String? = null, notes: String? = null) {
        val cartItems = _cart.value?.toList() ?: return
        if (cartItems.isEmpty()) return

        viewModelScope.launch {
            try {
                val transactionId = java.util.UUID.randomUUID().toString()
                val paymentMethod = _selectedPaymentMethod.value ?: "Cash"
                val discountAmt = _discount.value ?: 0.0
                val now = LocalDateTime.now().toString()

                // Build sale records
                val sales = cartItems.map { item ->
                    Sale(
                        productId = item.productId,
                        productName = item.productName,
                        category = item.category,
                        quantitySold = item.quantity,
                        sellingPrice = item.sellingPrice,
                        costPrice = item.costPrice,
                        totalAmount = item.totalAmount,
                        profit = item.totalProfit,
                        discount = if (cartItems.size == 1) discountAmt else 0.0,
                        paymentMethod = paymentMethod,
                        notes = notes,
                        saleDate = now,
                        transactionId = transactionId
                    )
                }

                val totalAmt = sales.sumOf { it.totalAmount } - discountAmt
                val totalProfit = sales.sumOf { it.profit }

                // Insert transaction header
                saleRepository.insertTransaction(
                    SaleTransaction(
                        transactionId = transactionId,
                        totalAmount = totalAmt,
                        totalProfit = totalProfit,
                        discount = discountAmt,
                        paymentMethod = paymentMethod,
                        customerName = customerName,
                        notes = notes,
                        createdAt = now
                    )
                )

                // Insert sale line items
                saleRepository.insertSales(sales)

                // Deduct stock
                cartItems.forEach { item ->
                    productRepository.decreaseStock(item.productId, item.quantity)
                    val product = productRepository.getProductById(item.productId)
                    if (product != null && product.isLowStock) {
                        val alertCount = alertRepository.getActiveAlertCountForProduct(product.id)
                        if (alertCount == 0) {
                            alertRepository.insertAlert(
                                RestockAlert(
                                    productId = product.id,
                                    productName = product.name,
                                    currentQuantity = product.quantity,
                                    threshold = product.minStockThreshold
                                )
                            )
                        }
                    }
                }

                _saleResult.value = SaleResult(
                    success = true,
                    transactionId = transactionId,
                    totalAmount = totalAmt,
                    profit = totalProfit,
                    itemCount = cartItems.sumOf { it.quantity }
                )

                clearCart()
                loadTodaySales()
            } catch (e: Exception) {
                _saleResult.value = SaleResult(success = false, error = e.message)
            }
        }
    }

    private fun loadTodaySales() {
        val (start, end) = DateUtils.todayRange()
        saleRepository.getSalesByDateRange(start, end).observeForever { sales ->
            _todaySales.value = sales
        }
    }

    fun clearSaleResult() { _saleResult.value = null }
}

data class SaleResult(
    val success: Boolean,
    val transactionId: String = "",
    val totalAmount: Double = 0.0,
    val profit: Double = 0.0,
    val itemCount: Int = 0,
    val error: String? = null
)
