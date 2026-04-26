package com.shopmate.viewmodels

import androidx.lifecycle.*
import com.shopmate.data.repository.ProductRepository
import com.shopmate.data.repository.SaleRepository
import com.shopmate.data.repository.UserRepository
import com.shopmate.models.CategorySales
import com.shopmate.models.DailySales
import com.shopmate.models.SalesSummary
import com.shopmate.utils.DateUtils
import kotlinx.coroutines.launch

class ReportsViewModel(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    enum class Period { TODAY, WEEK, MONTH }

    private val _period = MutableLiveData(Period.TODAY)
    val period: LiveData<Period> = _period

    private val _summary = MutableLiveData<SalesSummary>()
    val summary: LiveData<SalesSummary> = _summary

    private val _dailyChart = MutableLiveData<List<DailySales>>()
    val dailyChart: LiveData<List<DailySales>> = _dailyChart

    private val _categoryBreakdown = MutableLiveData<List<CategorySales>>()
    val categoryBreakdown: LiveData<List<CategorySales>> = _categoryBreakdown

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _topProducts = MutableLiveData<List<com.shopmate.data.dao.TopProduct>>()
    val topProducts: LiveData<List<com.shopmate.data.dao.TopProduct>> = _topProducts

    private val _totalInventoryValue = MutableLiveData(0.0)
    val totalInventoryValue: LiveData<Double> = _totalInventoryValue

    init { loadReports(Period.TODAY) }

    fun setPeriod(p: Period) {
        _period.value = p
        loadReports(p)
    }

    fun loadReports(p: Period = _period.value ?: Period.TODAY) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val (start, end) = when (p) {
                    Period.TODAY -> DateUtils.todayRange()
                    Period.WEEK -> DateUtils.weekRange()
                    Period.MONTH -> DateUtils.monthRange()
                }
                _summary.value = saleRepository.getSummaryForRange(start, end)
                _dailyChart.value = saleRepository.getDailySalesChart(start, end)
                _categoryBreakdown.value = saleRepository.getSalesByCategory(start, end)
                _topProducts.value = saleRepository.getTopSellingProducts(start)
                _totalInventoryValue.value = productRepository.getTotalInventoryValue()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun checkInitialState() {
        viewModelScope.launch {
            val hasUsers = userRepository.hasUsers()
            val isSkip = userRepository.isSkipLogin()
            val isLoggedIn = userRepository.isLoggedIn()
            _authState.value = when {
                !hasUsers -> AuthState.SETUP
                isSkip || isLoggedIn -> AuthState.AUTHENTICATED
                else -> AuthState.LOGIN
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val user = userRepository.verifyCredentials(username, password)
            if (user != null) {
                userRepository.setLoggedIn(true)
                userRepository.setShopName(user.shopName)
                _authState.value = AuthState.AUTHENTICATED
            } else {
                _authState.value = AuthState.LOGIN_FAILED
            }
            _isLoading.value = false
        }
    }

    fun setupAccount(username: String, password: String, shopName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userRepository.createUser(username, password, shopName)
                userRepository.setLoggedIn(true)
                userRepository.setShopName(shopName)
                _authState.value = AuthState.AUTHENTICATED
            } catch (e: Exception) {
                _authState.value = AuthState.SETUP_FAILED
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun skipLogin(shopName: String) {
        viewModelScope.launch {
            userRepository.setSkipLogin(true)
            userRepository.setShopName(shopName)
            _authState.value = AuthState.AUTHENTICATED
        }
    }

    fun isFirstLaunch(): Boolean = userRepository.isFirstLaunch()
    fun setFirstLaunchDone() = userRepository.setFirstLaunch(false)
    fun getShopName(): String = userRepository.getShopName()
}

enum class AuthState {
    SETUP, LOGIN, AUTHENTICATED, LOGIN_FAILED, SETUP_FAILED
}

class BackupViewModel(
    private val productRepository: ProductRepository,
    private val saleRepository: SaleRepository
) : ViewModel() {
    private val _result = MutableLiveData<String?>()
    val result: LiveData<String?> = _result

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun exportProducts(context: android.content.Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val products = productRepository.getAllProductsSync()
            val r = com.shopmate.utils.ExportHelper.exportProductsToCSV(context, products)
            _result.value = r.message
            _isLoading.value = false
        }
    }

    fun exportSales(context: android.content.Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val sales = saleRepository.getAllSalesSync()
            val r = com.shopmate.utils.ExportHelper.exportSalesToCSV(context, sales)
            _result.value = r.message
            _isLoading.value = false
        }
    }

    fun fullBackup(context: android.content.Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val products = productRepository.getAllProductsSync()
            val sales = saleRepository.getAllSalesSync()
            val r = com.shopmate.utils.ExportHelper.backupToJSON(context, products, sales)
            _result.value = r.message
            _isLoading.value = false
        }
    }

    fun loadSampleData(externalSaleRepo: SaleRepository? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            val salesRepo = externalSaleRepo ?: saleRepository
            com.shopmate.utils.SampleDataHelper.seedAll(productRepository, salesRepo)
            _result.value = "Sample products and 14 days of sales data loaded!"
            _isLoading.value = false
        }
    }

    fun clearResult() { _result.value = null }
}
