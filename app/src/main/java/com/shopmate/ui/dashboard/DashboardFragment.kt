package com.shopmate.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.shopmate.R
import com.shopmate.ShopMateApp
import com.shopmate.adapters.LowStockAdapter
import com.shopmate.databinding.FragmentDashboardBinding
import com.shopmate.ui.inventory.AddEditProductActivity
import com.shopmate.ui.sales.RecordSaleActivity
import com.shopmate.utils.gone
import com.shopmate.utils.toCurrency
import com.shopmate.utils.visible
import com.shopmate.viewmodels.DashboardViewModel
import com.shopmate.viewmodels.ViewModelFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { requireActivity().application as ShopMateApp }
    private val viewModel: DashboardViewModel by viewModels {
        ViewModelFactory(app.productRepository, app.saleRepository, app.userRepository, app.alertRepository)
    }

    private lateinit var lowStockAdapter: LowStockAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupViews() {
        val now = LocalDateTime.now()
        val hour = now.hour
        val greeting = when {
            hour < 12 -> "Good Morning!"
            hour < 17 -> "Good Afternoon!"
            else -> "Good Evening!"
        }
        binding.tvGreeting.text = greeting
        binding.tvShopName.text = app.userRepository.getShopName()

        val dateFormat = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy", Locale.getDefault())
        binding.tvDate.text = LocalDate.now().format(dateFormat)
    }

    private fun setupRecyclerView() {
        lowStockAdapter = LowStockAdapter(
            onRestockClick = { product ->
                val intent = Intent(requireContext(), AddEditProductActivity::class.java).apply {
                    putExtra(AddEditProductActivity.EXTRA_PRODUCT_ID, product.id)
                    putExtra(AddEditProductActivity.EXTRA_RESTOCK_MODE, true)
                }
                startActivity(intent)
            }
        )
        binding.rvLowStock.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = lowStockAdapter
        }
    }

    private fun setupObservers() {
        viewModel.totalProducts.observe(viewLifecycleOwner) { count ->
            binding.tvTotalProducts.text = count?.toString() ?: "0"
        }

        viewModel.lowStockCount.observe(viewLifecycleOwner) { count ->
            binding.tvLowStock.text = count?.toString() ?: "0"
        }

        viewModel.dashboardData.observe(viewLifecycleOwner) { data ->
            data ?: return@observe
            binding.tvTodaySales.text = data.todayRevenue.toCurrency()
            binding.tvRevenue.text = data.todayRevenue.toCurrency()
            binding.tvInventoryValue.text = data.totalInventoryValue.toCurrency()
            binding.tvTransactionCount.text = "${data.todayTransactions} transactions"
        }

        viewModel.lowStockProducts.observe(viewLifecycleOwner) { products ->
            val visible = products?.isNotEmpty() == true
            binding.layoutLowStockHeader.visibility = if (visible) View.VISIBLE else View.GONE
            binding.rvLowStock.visibility = if (visible) View.VISIBLE else View.GONE
            if (visible) lowStockAdapter.submitList(products?.take(5))
        }
    }

    private fun setupClickListeners() {
        binding.cardAddProduct.setOnClickListener {
            startActivity(Intent(requireContext(), AddEditProductActivity::class.java))
        }

        binding.cardRecordSale.setOnClickListener {
            startActivity(Intent(requireContext(), RecordSaleActivity::class.java))
        }

        binding.cardInventory.setOnClickListener {
            findNavController().navigate(R.id.inventoryFragment)
        }

        binding.cardReports.setOnClickListener {
            findNavController().navigate(R.id.reportsFragment)
        }

        binding.tvSeeAll.setOnClickListener {
            findNavController().navigate(R.id.inventoryFragment)
        }

        binding.btnNotifications.setOnClickListener {
            startActivity(Intent(requireContext(), com.shopmate.ui.settings.SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadDashboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
