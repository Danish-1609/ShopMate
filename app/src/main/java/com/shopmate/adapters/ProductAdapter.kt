package com.shopmate.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shopmate.R
import com.shopmate.data.entities.Product
import com.shopmate.data.entities.StockStatus
import com.shopmate.databinding.ItemProductBinding
import com.shopmate.utils.toCurrency

class ProductAdapter(
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit,
    private val onRestockClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(a: Product, b: Product) = a.id == b.id
        override fun areContentsTheSame(a: Product, b: Product) = a == b
    }

    inner class ViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvCategory.text = product.category
            binding.tvQuantity.text = "${product.quantity} ${product.unit}"
            binding.tvSellingPrice.text = product.sellingPrice.toCurrency()
            binding.tvCostPrice.text = product.costPrice.toCurrency()

            if (!product.barcode.isNullOrEmpty()) {
                binding.tvBarcode.text = "SKU: ${product.barcode}"
                binding.tvBarcode.visibility = android.view.View.VISIBLE
            } else {
                binding.tvBarcode.visibility = android.view.View.GONE
            }

            // Stock status badge & indicator
            val (statusText, statusBg, indicatorColor) = when (product.stockStatus) {
                StockStatus.OUT_OF_STOCK -> Triple(
                    "Out of Stock",
                    R.drawable.bg_badge_error,
                    R.color.stock_out
                )
                StockStatus.LOW_STOCK -> Triple(
                    "Low Stock",
                    R.drawable.bg_badge_warning,
                    R.color.stock_low
                )
                StockStatus.IN_STOCK -> Triple(
                    "In Stock",
                    R.drawable.bg_badge_success,
                    R.color.stock_good
                )
            }
            binding.tvStockStatus.text = statusText
            binding.tvStockStatus.setBackgroundResource(statusBg)
            binding.tvStockStatus.setTextColor(
                itemView.context.getColor(
                    when (product.stockStatus) {
                        StockStatus.OUT_OF_STOCK -> R.color.error
                        StockStatus.LOW_STOCK -> R.color.warning
                        StockStatus.IN_STOCK -> R.color.success
                    }
                )
            )
            binding.stockIndicator.setBackgroundColor(
                itemView.context.getColor(indicatorColor)
            )

            binding.btnEdit.setOnClickListener { onEditClick(product) }
            binding.btnDelete.setOnClickListener { onDeleteClick(product) }
            binding.btnRestock.setOnClickListener { onRestockClick(product) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
