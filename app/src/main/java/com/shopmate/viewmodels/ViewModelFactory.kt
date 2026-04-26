package com.shopmate.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shopmate.data.repository.*

class ViewModelFactory(
    private val productRepository: ProductRepository,
    private val saleRepository: SaleRepository,
    private val userRepository: UserRepository,
    private val alertRepository: AlertRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(productRepository, saleRepository, alertRepository) as T
            modelClass.isAssignableFrom(InventoryViewModel::class.java) ->
                InventoryViewModel(productRepository, alertRepository) as T
            modelClass.isAssignableFrom(SalesViewModel::class.java) ->
                SalesViewModel(saleRepository, productRepository, alertRepository) as T
            modelClass.isAssignableFrom(ReportsViewModel::class.java) ->
                ReportsViewModel(saleRepository, productRepository) as T
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(userRepository) as T
            modelClass.isAssignableFrom(BackupViewModel::class.java) ->
                BackupViewModel(productRepository, saleRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
