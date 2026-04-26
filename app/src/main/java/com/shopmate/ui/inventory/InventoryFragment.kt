package com.shopmate.ui.inventory

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.shopmate.R
import com.shopmate.ShopMateApp
import com.shopmate.adapters.ProductAdapter
import com.shopmate.databinding.FragmentInventoryBinding
import com.shopmate.models.FilterOption
import com.shopmate.models.SortOption
import com.shopmate.utils.gone
import com.shopmate.utils.onTextChanged
import com.shopmate.utils.toast
import com.shopmate.utils.visible
import com.shopmate.viewmodels.InventoryViewModel
import com.shopmate.viewmodels.ViewModelFactory

class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { requireActivity().application as ShopMateApp }
    private val viewModel: InventoryViewModel by viewModels {
        ViewModelFactory(app.productRepository, app.saleRepository, app.userRepository, app.alertRepository)
    }
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupSearchAndFilter()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            onEditClick = { product ->
                val intent = Intent(requireContext(), AddEditProductActivity::class.java).apply {
                    putExtra(AddEditProductActivity.EXTRA_PRODUCT_ID, product.id)
                }
                startActivity(intent)
            },
            onDeleteClick = { product ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Product")
                    .setMessage("Are you sure you want to delete \"${product.name}\"? This cannot be undone.")
                    .setPositiveButton("Delete") { _, _ -> viewModel.deleteProduct(product) }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onRestockClick = { product ->
                showRestockDialog(product.id, product.name)
            }
        )
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
        }
    }

    private fun setupObservers() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            productAdapter.submitList(products)
            val empty = products.isNullOrEmpty()
            binding.layoutEmpty.visibility = if (empty) View.VISIBLE else View.GONE
            binding.rvProducts.visibility = if (empty) View.GONE else View.VISIBLE
            binding.tvProductCount.text = "${products?.size ?: 0} products"
        }

        viewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            setupCategoryChips(categories)
        }

        viewModel.operationResult.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                toast(it)
                viewModel.clearOperationResult()
            }
        }
    }

    private fun setupSearchAndFilter() {
        binding.etSearch.onTextChanged { viewModel.setSearch(it) }

        binding.chipAll.setOnCheckedChangeListener { _, checked ->
            if (checked) viewModel.setFilter(FilterOption.ALL)
        }
        binding.chipLowStock.setOnCheckedChangeListener { _, checked ->
            if (checked) viewModel.setFilter(FilterOption.LOW_STOCK)
        }
        binding.chipOutOfStock.setOnCheckedChangeListener { _, checked ->
            if (checked) viewModel.setFilter(FilterOption.OUT_OF_STOCK)
        }
        binding.chipInStock.setOnCheckedChangeListener { _, checked ->
            if (checked) viewModel.setFilter(FilterOption.IN_STOCK)
        }
    }

    private fun setupCategoryChips(categories: List<String>) {
        // Remove existing dynamic chips (keep the first "All Categories" chip if you have one)
        val existingCount = binding.chipGroupCategories.childCount
        if (existingCount > 0) binding.chipGroupCategories.removeAllViews()

        val allChip = Chip(requireContext()).apply {
            text = "All Categories"
            isCheckable = true
            isChecked = true
        }
        allChip.setOnCheckedChangeListener { _, checked ->
            if (checked) viewModel.setCategory(null)
        }
        binding.chipGroupCategories.addView(allChip)

        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
            }
            chip.setOnCheckedChangeListener { _, checked ->
                if (checked) viewModel.setCategory(category)
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    private fun setupClickListeners() {
        binding.fabAddProduct.setOnClickListener {
            startActivity(Intent(requireContext(), AddEditProductActivity::class.java))
        }

        binding.btnSort.setOnClickListener {
            showSortDialog()
        }

        binding.btnFilter.setOnClickListener {
            // The filter chips handle this inline
        }
    }

    private fun showSortDialog() {
        val options = arrayOf(
            "Name (A→Z)", "Name (Z→A)",
            "Price (Low→High)", "Price (High→Low)",
            "Stock (Low→High)", "Stock (High→Low)"
        )
        AlertDialog.Builder(requireContext())
            .setTitle("Sort By")
            .setItems(options) { _, which ->
                viewModel.setSort(
                    when (which) {
                        0 -> SortOption.NAME_ASC
                        1 -> SortOption.NAME_DESC
                        2 -> SortOption.PRICE_ASC
                        3 -> SortOption.PRICE_DESC
                        4 -> SortOption.STOCK_ASC
                        else -> SortOption.STOCK_DESC
                    }
                )
            }
            .show()
    }

    private fun showRestockDialog(productId: Long, productName: String) {
        val view = layoutInflater.inflate(R.layout.dialog_restock, null)
        val etAmount = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etRestockAmount)

        AlertDialog.Builder(requireContext())
            .setTitle("Restock: $productName")
            .setView(view)
            .setPositiveButton("Add Stock") { _, _ ->
                val amount = etAmount.text?.toString()?.toIntOrNull() ?: 0
                if (amount > 0) viewModel.restockProduct(productId, amount)
                else toast("Enter a valid amount")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Refresh product list
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
