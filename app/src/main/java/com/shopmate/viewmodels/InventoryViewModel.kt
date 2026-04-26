package com.shopmate.viewmodels

import androidx.lifecycle.*
import com.shopmate.data.entities.Product
import com.shopmate.data.entities.RestockAlert
import com.shopmate.data.repository.AlertRepository
import com.shopmate.data.repository.ProductRepository
import com.shopmate.models.FilterOption
import com.shopmate.models.SortOption
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class InventoryViewModel(
    private val productRepository: ProductRepository,
    private val alertRepository: AlertRepository
) : ViewModel() {

    private val _searchQuery = MutableLiveData("")
    private val _filterOption = MutableLiveData(FilterOption.ALL)
    private val _sortOption = MutableLiveData(SortOption.NAME_ASC)
    private val _selectedCategory = MutableLiveData<String?>(null)

    val searchQuery: LiveData<String> = _searchQuery
    val filterOption: LiveData<FilterOption> = _filterOption
    val sortOption: LiveData<SortOption> = _sortOption
    val selectedCategory: LiveData<String?> = _selectedCategory

    val allCategories = productRepository.getAllCategories()
    val lowStockCount = productRepository.getLowStockCount()

    private val _products = MediatorLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _operationResult = MutableLiveData<String?>()
    val operationResult: LiveData<String?> = _operationResult

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val baseProducts = productRepository.getAllProducts()

    init {
        _products.addSource(baseProducts) { applyFilters(it) }
        _products.addSource(_searchQuery) { applyFilters(baseProducts.value) }
        _products.addSource(_filterOption) { applyFilters(baseProducts.value) }
        _products.addSource(_sortOption) { applyFilters(baseProducts.value) }
        _products.addSource(_selectedCategory) { applyFilters(baseProducts.value) }
    }

    private fun applyFilters(raw: List<Product>?) {
        if (raw == null) return
        var list = raw

        val query = _searchQuery.value?.trim() ?: ""
        if (query.isNotEmpty()) {
            list = list.filter {
                it.name.contains(query, true) ||
                it.category.contains(query, true) ||
                it.barcode?.contains(query, true) == true
            }
        }

        val category = _selectedCategory.value
        if (!category.isNullOrEmpty()) {
            list = list.filter { it.category == category }
        }

        list = when (_filterOption.value) {
            FilterOption.LOW_STOCK -> list.filter { it.isLowStock && !it.isOutOfStock }
            FilterOption.OUT_OF_STOCK -> list.filter { it.isOutOfStock }
            FilterOption.IN_STOCK -> list.filter { !it.isLowStock }
            else -> list
        }

        list = when (_sortOption.value) {
            SortOption.NAME_ASC -> list.sortedBy { it.name }
            SortOption.NAME_DESC -> list.sortedByDescending { it.name }
            SortOption.PRICE_ASC -> list.sortedBy { it.sellingPrice }
            SortOption.PRICE_DESC -> list.sortedByDescending { it.sellingPrice }
            SortOption.STOCK_ASC -> list.sortedBy { it.quantity }
            SortOption.STOCK_DESC -> list.sortedByDescending { it.quantity }
            else -> list
        }

        _products.value = list
    }

    fun setSearch(query: String) { _searchQuery.value = query }
    fun setFilter(filter: FilterOption) { _filterOption.value = filter }
    fun setSort(sort: SortOption) { _sortOption.value = sort }
    fun setCategory(category: String?) { _selectedCategory.value = category }
    fun clearFilters() {
        _searchQuery.value = ""
        _filterOption.value = FilterOption.ALL
        _selectedCategory.value = null
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val id = productRepository.insertProduct(product)
                if (product.isLowStock) triggerRestockAlert(product.copy(id = id))
                _operationResult.value = "Product added successfully"
            } catch (e: Exception) {
                _operationResult.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                productRepository.updateProduct(product)
                if (product.isLowStock) triggerRestockAlert(product)
                else alertRepository.acknowledgeAlertsByProduct(product.id)
                _operationResult.value = "Product updated successfully"
            } catch (e: Exception) {
                _operationResult.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            try {
                productRepository.deleteProduct(product)
                alertRepository.acknowledgeAlertsByProduct(product.id)
                _operationResult.value = "Product deleted"
            } catch (e: Exception) {
                _operationResult.value = "Error: ${e.message}"
            }
        }
    }

    fun restockProduct(productId: Long, amount: Int) {
        viewModelScope.launch {
            try {
                productRepository.increaseStock(productId, amount)
                alertRepository.acknowledgeAlertsByProduct(productId)
                _operationResult.value = "Stock updated"
            } catch (e: Exception) {
                _operationResult.value = "Error: ${e.message}"
            }
        }
    }

    private suspend fun triggerRestockAlert(product: Product) {
        val existing = alertRepository.getActiveAlertCountForProduct(product.id)
        if (existing == 0) {
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

    fun clearOperationResult() { _operationResult.value = null }
}
