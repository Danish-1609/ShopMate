package com.shopmate.ui.reports

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.tabs.TabLayout
import com.shopmate.ShopMateApp
import com.shopmate.adapters.CategoryBreakdownAdapter
import com.shopmate.databinding.FragmentReportsBinding
import com.shopmate.utils.DateUtils
import com.shopmate.utils.ExportHelper
import com.shopmate.utils.toCurrency
import com.shopmate.utils.toast
import com.shopmate.viewmodels.BackupViewModel
import com.shopmate.viewmodels.ReportsViewModel
import com.shopmate.viewmodels.ViewModelFactory

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { requireActivity().application as ShopMateApp }
    private val viewModel: ReportsViewModel by viewModels {
        ViewModelFactory(app.productRepository, app.saleRepository, app.userRepository, app.alertRepository)
    }
    private val backupViewModel: BackupViewModel by viewModels {
        ViewModelFactory(app.productRepository, app.saleRepository, app.userRepository, app.alertRepository)
    }

    private lateinit var categoryAdapter: CategoryBreakdownAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs()
        setupRecyclerView()
        setupCharts()
        setupObservers()
        setupClickListeners()
        binding.tvExportPath.text = "Exports saved to: ${ExportHelper.getExportDirPath(requireContext())}"
    }

    private fun setupTabs() {
        binding.tabPeriod.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.setPeriod(
                    when (tab.position) {
                        0 -> ReportsViewModel.Period.TODAY
                        1 -> ReportsViewModel.Period.WEEK
                        else -> ReportsViewModel.Period.MONTH
                    }
                )
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryBreakdownAdapter()
        binding.rvCategoryBreakdown.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupCharts() {
        // Line chart styling
        binding.lineChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            setDrawGridBackground(false)
            axisRight.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(true)
            animateX(800)
        }

        // Pie chart styling
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 45f
            setEntryLabelTextSize(11f)
            animateY(800)
        }

        // Bar chart styling
        binding.barChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            axisRight.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            animateY(800)
        }
    }

    private fun setupObservers() {
        viewModel.summary.observe(viewLifecycleOwner) { summary ->
            summary ?: return@observe
            binding.tvRevenue.text = summary.totalRevenue.toCurrency()
            binding.tvProfit.text = summary.totalProfit.toCurrency()
            binding.tvTransactions.text = summary.transactionCount.toString()
        }

        viewModel.totalInventoryValue.observe(viewLifecycleOwner) { value ->
            binding.tvInventoryValue.text = value.toCurrency()
        }

        viewModel.dailyChart.observe(viewLifecycleOwner) { dailySales ->
            if (dailySales.isNullOrEmpty()) {
                binding.lineChart.clear()
                binding.lineChart.setNoDataText("No sales data for this period")
                return@observe
            }
            val entries = dailySales.mapIndexed { i, d -> Entry(i.toFloat(), d.totalAmount.toFloat()) }
            val labels = dailySales.map { DateUtils.formatDate(it.date) }

            val dataSet = LineDataSet(entries, "Revenue").apply {
                color = resources.getColor(com.shopmate.R.color.primary, null)
                setCircleColor(resources.getColor(com.shopmate.R.color.primary, null))
                lineWidth = 2.5f
                circleRadius = 4f
                setDrawFilled(true)
                fillColor = resources.getColor(com.shopmate.R.color.primary_light, null)
                fillAlpha = 60
                valueTextSize = 9f
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            binding.lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            binding.lineChart.data = LineData(dataSet)
            binding.lineChart.invalidate()
        }

        viewModel.categoryBreakdown.observe(viewLifecycleOwner) { categories ->
            if (categories.isNullOrEmpty()) {
                binding.pieChart.clear()
                binding.pieChart.setNoDataText("No category data")
                categoryAdapter.submitList(emptyList())
                return@observe
            }

            val total = categories.sumOf { it.totalAmount }
            val colors = listOf(
                Color.parseColor("#1565C0"), Color.parseColor("#00897B"),
                Color.parseColor("#FF6F00"), Color.parseColor("#6A1B9A"),
                Color.parseColor("#AD1457"), Color.parseColor("#558B2F"),
                Color.parseColor("#0277BD"), Color.parseColor("#E65100")
            )

            val pieEntries = categories.mapIndexed { i, c ->
                PieEntry(c.totalAmount.toFloat(), c.category)
            }
            val pieDataSet = PieDataSet(pieEntries, "").apply {
                this.colors = colors.take(categories.size)
                valueTextSize = 11f
                valueTextColor = Color.WHITE
                sliceSpace = 2f
            }
            binding.pieChart.data = PieData(pieDataSet)
            binding.pieChart.invalidate()

            // Category list with percentages
            categoryAdapter.submitList(categories.map { cat ->
                Triple(cat.category, cat.totalAmount, if (total > 0) (cat.totalAmount / total * 100).toInt() else 0)
            }.zip(colors.take(categories.size)))
        }

        viewModel.topProducts.observe(viewLifecycleOwner) { products ->
            if (products.isNullOrEmpty()) {
                binding.barChart.clear()
                binding.barChart.setNoDataText("No sales data")
                return@observe
            }
            val entries = products.mapIndexed { i, p -> BarEntry(i.toFloat(), p.totalQty.toFloat()) }
            val labels = products.map { it.product_name.take(12) }

            val dataSet = BarDataSet(entries, "Units Sold").apply {
                colors = ColorTemplate.MATERIAL_COLORS.toList()
                valueTextSize = 10f
            }
            binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            binding.barChart.xAxis.labelCount = products.size
            binding.barChart.xAxis.labelRotationAngle = -30f
            binding.barChart.data = BarData(dataSet)
            binding.barChart.invalidate()
        }

        backupViewModel.result.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                toast(it)
                backupViewModel.clearResult()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnExportProducts.setOnClickListener {
            backupViewModel.exportProducts(requireContext())
        }
        binding.btnExportSales.setOnClickListener {
            backupViewModel.exportSales(requireContext())
        }
        binding.btnBackup.setOnClickListener {
            backupViewModel.fullBackup(requireContext())
        }
        binding.btnExport.setOnClickListener {
            backupViewModel.exportSales(requireContext())
        }
        binding.btnLoadSample.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Load Sample Data")
                .setMessage("This will add 8 sample products and 14 days of sales history. Continue?")
                .setPositiveButton("Load") { _, _ ->
                    backupViewModel.loadSampleData()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
