package com.shopmate.ui.sales

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.shopmate.ShopMateApp
import com.shopmate.adapters.CartAdapter
import com.shopmate.adapters.SaleProductAdapter
import com.shopmate.databinding.ActivityRecordSaleBinding
import com.shopmate.utils.gone
import com.shopmate.utils.onTextChanged
import com.shopmate.utils.toCurrency
import com.shopmate.utils.toast
import com.shopmate.utils.visible
import com.shopmate.viewmodels.SalesViewModel
import com.shopmate.viewmodels.ViewModelFactory

class RecordSaleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordSaleBinding
    private val app by lazy { application as ShopMateApp }
    private val viewModel: SalesViewModel by viewModels {
        ViewModelFactory(app.productRepository, app.saleRepository, app.userRepository, app.alertRepository)
    }

    private lateinit var productAdapter: SaleProductAdapter
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordSaleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupProductList()
        setupCartList()
        setupObservers()
        setupClickListeners()
    }

    private fun setupProductList() {
        productAdapter = SaleProductAdapter { product ->
            viewModel.addToCart(product)
        }
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(this@RecordSaleActivity)
            adapter = productAdapter
        }
    }

    private fun setupCartList() {
        cartAdapter = CartAdapter(
            onQtyMinus = { item -> viewModel.updateCartItemQuantity(item.productId, item.quantity - 1) },
            onQtyPlus = { item -> viewModel.updateCartItemQuantity(item.productId, item.quantity + 1) },
            onRemove = { item -> viewModel.removeFromCart(item.productId) }
        )
        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(this@RecordSaleActivity)
            adapter = cartAdapter
        }
    }

    private fun setupObservers() {
        viewModel.filteredProducts.observe(this) { products ->
            productAdapter.submitList(products)
        }

        viewModel.cart.observe(this) { cart ->
            val isEmpty = cart.isNullOrEmpty()
            binding.cardCart.visibility = if (isEmpty) android.view.View.GONE else android.view.View.VISIBLE
            if (!isEmpty) {
                cartAdapter.submitList(cart.toList())
                binding.tvCartItems.text = "${cart.sumOf { it.quantity }} items"
            }
            val count = cart?.sumOf { it.quantity } ?: 0
            binding.btnViewCart.text = "Cart ($count)"
        }

        viewModel.cartTotal.observe(this) { total ->
            binding.tvTotal.text = (total ?: 0.0).toCurrency()
        }

        viewModel.saleResult.observe(this) { result ->
            result ?: return@observe
            if (result.success) {
                showSuccessDialog(result.totalAmount, result.itemCount)
            } else {
                toast("Sale failed: ${result.error}")
            }
            viewModel.clearSaleResult()
        }
    }

    private fun setupClickListeners() {
        binding.etSearch.onTextChanged { viewModel.setSearch(it) }

        binding.chipCash.setOnCheckedChangeListener { _, c -> if (c) viewModel.setPaymentMethod("Cash") }
        binding.chipUpi.setOnCheckedChangeListener { _, c -> if (c) viewModel.setPaymentMethod("UPI") }
        binding.chipCard.setOnCheckedChangeListener { _, c -> if (c) viewModel.setPaymentMethod("Card") }

        binding.etDiscount.onTextChanged { text ->
            viewModel.setDiscount(text.toDoubleOrNull() ?: 0.0)
        }

        binding.btnCompleteSale.setOnClickListener {
            val cart = viewModel.cart.value
            if (cart.isNullOrEmpty()) {
                toast("Add products to cart first")
                return@setOnClickListener
            }
            viewModel.completeSale()
        }

        binding.btnViewCart.setOnClickListener {
            binding.cardCart.visibility = if (binding.cardCart.visibility == android.view.View.VISIBLE)
                android.view.View.GONE else android.view.View.VISIBLE
        }
    }

    private fun showSuccessDialog(total: Double, items: Int) {
        AlertDialog.Builder(this)
            .setTitle("✓ Sale Complete!")
            .setMessage("Successfully sold $items item(s)\nTotal: ${total.toCurrency()}")
            .setPositiveButton("New Sale") { _, _ -> /* Stay on screen */ }
            .setNegativeButton("Done") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }
}
