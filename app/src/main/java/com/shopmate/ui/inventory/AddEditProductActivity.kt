package com.shopmate.ui.inventory

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.shopmate.R
import com.shopmate.ShopMateApp
import com.shopmate.data.entities.Product
import com.shopmate.databinding.ActivityAddEditProductBinding
import com.shopmate.utils.toast
import com.shopmate.utils.visible
import com.shopmate.utils.gone
import com.shopmate.viewmodels.InventoryViewModel
import com.shopmate.viewmodels.ViewModelFactory
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class AddEditProductActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRODUCT_ID = "product_id"
        const val EXTRA_RESTOCK_MODE = "restock_mode"
    }

    private lateinit var binding: ActivityAddEditProductBinding
    private val app by lazy { application as ShopMateApp }
    private val viewModel: InventoryViewModel by viewModels {
        ViewModelFactory(app.productRepository, app.saleRepository, app.userRepository, app.alertRepository)
    }

    private var editingProduct: Product? = null
    private var isRestockMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isRestockMode = intent.getBooleanExtra(EXTRA_RESTOCK_MODE, false)
        val productId = intent.getLongExtra(EXTRA_PRODUCT_ID, -1L)

        setupToolbar(productId != -1L)
        setupDropdowns()
        setupPriceWatcher()
        setupObservers()

        if (productId != -1L) loadProduct(productId)

        binding.fabSave.setOnClickListener { saveProduct() }
        binding.tilBarcode.setEndIconOnClickListener { showBarcodeDialog() }
    }

    private fun setupToolbar(isEdit: Boolean) {
        binding.toolbar.apply {
            title = when {
                isRestockMode -> "Restock Product"
                isEdit -> "Edit Product"
                else -> "Add Product"
            }
            setSupportActionBar(this)
            setNavigationOnClickListener { finish() }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupDropdowns() {
        val categories = resources.getStringArray(R.array.default_categories).toList()
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(categoryAdapter)

        val units = resources.getStringArray(R.array.units).toList()
        val unitAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, units)
        binding.actvUnit.setAdapter(unitAdapter)
        binding.actvUnit.setText("pcs", false)
    }

    private fun setupPriceWatcher() {
        val watcher = { _: String ->
            val cost = binding.etCostPrice.text?.toString()?.toDoubleOrNull() ?: 0.0
            val sell = binding.etSellingPrice.text?.toString()?.toDoubleOrNull() ?: 0.0
            if (cost > 0 && sell > 0) {
                val margin = ((sell - cost) / cost) * 100
                binding.tvProfitMargin.text = String.format("%.1f%%", margin)
                binding.cardProfitPreview.visible()
            } else {
                binding.cardProfitPreview.gone()
            }
        }
        binding.etCostPrice.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { watcher(s.toString()) }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        binding.etSellingPrice.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { watcher(s.toString()) }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupObservers() {
        viewModel.operationResult.observe(this) { msg ->
            msg?.let {
                toast(it)
                viewModel.clearOperationResult()
                if (it.contains("successfully", true)) finish()
            }
        }
    }

    private fun loadProduct(id: Long) {
        lifecycleScope.launch {
            val product = app.productRepository.getProductById(id)
            product?.let { p ->
                editingProduct = p
                binding.etProductName.setText(p.name)
                binding.actvCategory.setText(p.category, false)
                binding.etQuantity.setText(p.quantity.toString())
                binding.actvUnit.setText(p.unit, false)
                binding.etMinThreshold.setText(p.minStockThreshold.toString())
                binding.etCostPrice.setText(p.costPrice.toString())
                binding.etSellingPrice.setText(p.sellingPrice.toString())
                binding.etBarcode.setText(p.barcode ?: "")
                binding.etDescription.setText(p.description ?: "")

                if (isRestockMode) {
                    // Focus on quantity
                    binding.etQuantity.requestFocus()
                    binding.etQuantity.selectAll()
                }
            }
        }
    }

    private fun saveProduct() {
        if (!validateInputs()) return

        val name = binding.etProductName.text.toString().trim()
        val category = binding.actvCategory.text.toString().trim().ifEmpty { "Other" }
        val quantity = binding.etQuantity.text.toString().toIntOrNull() ?: 0
        val unit = binding.actvUnit.text.toString().trim().ifEmpty { "pcs" }
        val minThreshold = binding.etMinThreshold.text.toString().toIntOrNull() ?: 0
        val costPrice = binding.etCostPrice.text.toString().toDoubleOrNull() ?: 0.0
        val sellingPrice = binding.etSellingPrice.text.toString().toDoubleOrNull() ?: 0.0
        val barcode = binding.etBarcode.text.toString().trim().takeIf { it.isNotEmpty() }
        val description = binding.etDescription.text.toString().trim().takeIf { it.isNotEmpty() }
        val now = LocalDateTime.now().toString()

        val product = editingProduct?.copy(
            name = name, category = category, quantity = quantity, unit = unit,
            minStockThreshold = minThreshold, costPrice = costPrice,
            sellingPrice = sellingPrice, barcode = barcode, description = description,
            updatedAt = now
        ) ?: Product(
            name = name, category = category, quantity = quantity, unit = unit,
            minStockThreshold = minThreshold, costPrice = costPrice,
            sellingPrice = sellingPrice, barcode = barcode, description = description
        )

        if (editingProduct != null) viewModel.updateProduct(product)
        else viewModel.addProduct(product)
    }

    private fun validateInputs(): Boolean {
        var valid = true
        binding.tilProductName.error = null
        binding.tilQuantity.error = null
        binding.tilSellingPrice.error = null

        if (binding.etProductName.text.isNullOrBlank()) {
            binding.tilProductName.error = "Product name is required"
            valid = false
        }
        if (binding.etQuantity.text.isNullOrBlank()) {
            binding.tilQuantity.error = "Quantity is required"
            valid = false
        }
        if (binding.etSellingPrice.text.isNullOrBlank()) {
            binding.tilSellingPrice.error = "Selling price is required"
            valid = false
        }
        val cost = binding.etCostPrice.text?.toString()?.toDoubleOrNull() ?: 0.0
        val sell = binding.etSellingPrice.text?.toString()?.toDoubleOrNull() ?: 0.0
        if (sell > 0 && cost > sell) {
            binding.tilCostPrice.error = "Cost price shouldn't exceed selling price"
            valid = false
        }
        return valid
    }

    private fun showBarcodeDialog() {
        val input = com.google.android.material.textfield.TextInputEditText(this)
        input.hint = "Enter barcode number"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Enter Barcode")
            .setMessage("Type or scan the product barcode")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val code = input.text?.toString()?.trim()
                if (!code.isNullOrEmpty()) binding.etBarcode.setText(code)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
