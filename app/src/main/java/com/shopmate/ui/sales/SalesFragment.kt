package com.shopmate.ui.sales

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.shopmate.ShopMateApp
import com.shopmate.adapters.SaleTransactionAdapter
import com.shopmate.databinding.FragmentSalesBinding
import com.shopmate.utils.DateUtils
import com.shopmate.utils.gone
import com.shopmate.utils.toCurrency
import com.shopmate.utils.visible
import com.shopmate.viewmodels.SalesViewModel
import com.shopmate.viewmodels.ViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class SalesFragment : Fragment() {

    private var _binding: FragmentSalesBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { requireActivity().application as ShopMateApp }
    private val viewModel: SalesViewModel by viewModels {
        ViewModelFactory(app.productRepository, app.saleRepository, app.userRepository, app.alertRepository)
    }

    private lateinit var salesAdapter: SaleTransactionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
        binding.tvTodayDate.text = LocalDate.now().format(formatter)
    }

    private fun setupRecyclerView() {
        salesAdapter = SaleTransactionAdapter()
        binding.rvSales.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = salesAdapter
        }
    }

    private fun setupObservers() {
        viewModel.todaySales.observe(viewLifecycleOwner) { sales ->
            salesAdapter.submitList(sales)
            val empty = sales.isNullOrEmpty()
            binding.layoutEmpty.visibility = if (empty) View.VISIBLE else View.GONE
            binding.rvSales.visibility = if (empty) View.GONE else View.VISIBLE

            val revenue = sales?.sumOf { it.totalAmount } ?: 0.0
            binding.tvTodayRevenue.text = revenue.toCurrency()
            binding.tvTodayTransactions.text = (sales?.size ?: 0).toString()
        }
    }

    private fun setupClickListeners() {
        binding.btnNewSale.setOnClickListener {
            startActivity(Intent(requireContext(), RecordSaleActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
