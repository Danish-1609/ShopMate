package com.shopmate.viewmodels

import androidx.lifecycle.*
import com.shopmate.data.repository.AlertRepository
import com.shopmate.data.repository.ProductRepository
import com.shopmate.data.repository.SaleRepository
import com.shopmate.models.DashboardData
import com.shopmate.utils.DateUtils
import kotlinx.coroutines.launch
import java.time.LocalDate

class DashboardViewModel(
    private val productRepository: ProductRepository,
    private val saleRepository: SaleRepository,
    private val alertRepository: AlertRepository
) : ViewModel() {

    val totalProducts = productRepository.getTotalProductCount()
    val lowStockCount = productRepository.getLowStockCount()
    val lowStockProducts = productRepository.getLowStockProducts()
    val activeAlertCount = alertRepository.getActiveAlertCount()

    private val _todayRevenue = MutableLiveData(0.0)
    val todayRevenue: LiveData<Double> = _todayRevenue

    private val _todayTransactions = MutableLiveData(0)
    val todayTransactions: LiveData<Int> = _todayTransactions

    private val _dashboardData = MutableLiveData<DashboardData>()
    val dashboardData: LiveData<DashboardData> = _dashboardData

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val todayDate: String = LocalDate.now().toString()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val (start, end) = DateUtils.todayRange()
                val summary = saleRepository.getSummaryForRange(start, end)
                val allProducts = productRepository.getAllProductsSync()
                val lowStock = productRepository.getLowStockProductsSync()

                _todayRevenue.value = summary.totalRevenue
                _todayTransactions.value = summary.transactionCount

                _dashboardData.value = DashboardData(
                    totalProducts = allProducts.size,
                    lowStockCount = lowStock.size,
                    todayRevenue = summary.totalRevenue,
                    todayTransactions = summary.transactionCount,
                    outOfStockCount = allProducts.count { it.isOutOfStock },
                    totalInventoryValue = allProducts.sumOf { it.costPrice * it.quantity }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun acknowledgeAlert(alertId: Long) {
        viewModelScope.launch {
            alertRepository.acknowledgeAlert(alertId)
        }
    }
}
