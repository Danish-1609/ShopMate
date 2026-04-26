package com.shopmate.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shopmate.data.entities.Product
import com.shopmate.data.entities.Sale
import com.shopmate.databinding.*
import com.shopmate.models.CartItem
import com.shopmate.utils.DateUtils
import com.shopmate.utils.toCurrency

// ─── Sale Product Picker ───────────────────────────────────────────────────

class SaleProductAdapter(
    private val onAddToCart: (Product) -> Unit
) : ListAdapter<Product, SaleProductAdapter.ViewHolder>(ProductDiff) {

    companion object ProductDiff : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(a: Product, b: Product) = a.id == b.id
        override fun areContentsTheSame(a: Product, b: Product) = a == b
    }

    inner class ViewHolder(private val b: ItemSaleProductBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(p: Product) {
            b.tvProductName.text = p.name
            b.tvCategory.text = p.category
            b.tvStock.text = "${p.quantity} ${p.unit}"
            b.tvPrice.text = p.sellingPrice.toCurrency()
            b.btnAddToCart.setOnClickListener { onAddToCart(p) }
            b.btnAddToCart.isEnabled = p.quantity > 0
            b.btnAddToCart.alpha = if (p.quantity > 0) 1f else 0.4f
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemSaleProductBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))
}

// ─── Cart Adapter ─────────────────────────────────────────────────────────

class CartAdapter(
    private val onQtyMinus: (CartItem) -> Unit,
    private val onQtyPlus: (CartItem) -> Unit,
    private val onRemove: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.ViewHolder>(CartDiff) {

    companion object CartDiff : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(a: CartItem, b: CartItem) = a.productId == b.productId
        override fun areContentsTheSame(a: CartItem, b: CartItem) = a == b
    }

    inner class ViewHolder(private val b: ItemCartBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: CartItem) {
            b.tvProductName.text = item.productName
            b.tvUnitPrice.text = "${item.sellingPrice.toCurrency()} each"
            b.tvQuantity.text = item.quantity.toString()
            b.tvLineTotal.text = item.totalAmount.toCurrency()
            b.btnMinus.setOnClickListener { onQtyMinus(item) }
            b.btnPlus.setOnClickListener { onQtyPlus(item) }
            b.btnRemove.setOnClickListener { onRemove(item) }
            b.btnPlus.isEnabled = item.quantity < item.maxQuantity
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))
}

// ─── Low Stock Alert Adapter ──────────────────────────────────────────────

class LowStockAdapter(
    private val onRestockClick: (com.shopmate.data.entities.Product) -> Unit
) : ListAdapter<com.shopmate.data.entities.Product, LowStockAdapter.ViewHolder>(LowStockDiff) {

    companion object LowStockDiff : DiffUtil.ItemCallback<com.shopmate.data.entities.Product>() {
        override fun areItemsTheSame(a: com.shopmate.data.entities.Product, b: com.shopmate.data.entities.Product) = a.id == b.id
        override fun areContentsTheSame(a: com.shopmate.data.entities.Product, b: com.shopmate.data.entities.Product) = a == b
    }

    inner class ViewHolder(private val b: ItemLowStockBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(p: com.shopmate.data.entities.Product) {
            b.tvProductName.text = p.name
            b.tvStockInfo.text = "${p.quantity} ${p.unit} left (min: ${p.minStockThreshold})"
            b.btnRestock.setOnClickListener { onRestockClick(p) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemLowStockBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))
}

// ─── Sale Transaction Adapter ─────────────────────────────────────────────

class SaleTransactionAdapter : ListAdapter<Sale, SaleTransactionAdapter.ViewHolder>(SaleDiff) {

    companion object SaleDiff : DiffUtil.ItemCallback<Sale>() {
        override fun areItemsTheSame(a: Sale, b: Sale) = a.id == b.id
        override fun areContentsTheSame(a: Sale, b: Sale) = a == b
    }

    inner class ViewHolder(private val b: ItemSaleTransactionBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(s: Sale) {
            b.tvProductName.text = s.productName
            b.tvQtySold.text = "x${s.quantitySold}"
            b.tvPaymentMethod.text = s.paymentMethod
            b.tvTime.text = try {
                val dt = java.time.LocalDateTime.parse(s.saleDate)
                dt.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))
            } catch (e: Exception) { "" }
            b.tvAmount.text = s.totalAmount.toCurrency()
            b.tvProfit.text = "+${s.profit.toCurrency()}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemSaleTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))
}

// ─── Category Breakdown Adapter ──────────────────────────────────────────

class CategoryBreakdownAdapter :
    ListAdapter<Pair<Triple<String, Double, Int>, Int>, CategoryBreakdownAdapter.ViewHolder>(CatDiff) {

    companion object CatDiff : DiffUtil.ItemCallback<Pair<Triple<String, Double, Int>, Int>>() {
        override fun areItemsTheSame(a: Pair<Triple<String, Double, Int>, Int>, b: Pair<Triple<String, Double, Int>, Int>) =
            a.first.first == b.first.first
        override fun areContentsTheSame(a: Pair<Triple<String, Double, Int>, Int>, b: Pair<Triple<String, Double, Int>, Int>) =
            a == b
    }

    inner class ViewHolder(private val b: ItemCategoryBreakdownBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: Pair<Triple<String, Double, Int>, Int>) {
            val (triple, color) = item
            val (category, amount, percent) = triple
            b.tvCategory.text = category
            b.tvAmount.text = amount.toCurrency()
            b.tvPercent.text = "$percent%"
            b.progressBar.progress = percent
            b.viewColor.setBackgroundColor(color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCategoryBreakdownBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))
}
