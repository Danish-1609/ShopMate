package com.shopmate.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.shopmate.ShopMateApp
import com.shopmate.databinding.ActivitySettingsBinding
import com.shopmate.ui.auth.AuthActivity
import com.shopmate.utils.toast

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val app by lazy { application as ShopMateApp }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        val repo = app.userRepository
        binding.tvShopNameValue.text = repo.getShopName()
        binding.switchDarkMode.isChecked = repo.isDarkMode()
        binding.switchNotifications.isChecked = repo.isNotificationsEnabled()
        binding.tvVersionValue.text = "1.0.0"
        binding.tvDbInfo.text = "Local SQLite via Room Database"
    }

    private fun setupListeners() {
        val repo = app.userRepository

        binding.rowShopName.setOnClickListener { showEditShopNameDialog() }

        binding.switchDarkMode.setOnCheckedChangeListener { _, checked ->
            repo.setDarkMode(checked)
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, checked ->
            repo.setNotificationsEnabled(checked)
        }

        binding.rowLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    repo.logout()
                    startActivity(Intent(this, AuthActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.rowAbout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("About ShopMate")
                .setMessage(
                    "ShopMate v1.0.0\n\n" +
                    "A fully offline inventory management app for retail shop owners.\n\n" +
                    "Built with:\n" +
                    "• Kotlin + MVVM Architecture\n" +
                    "• Room Database (SQLite)\n" +
                    "• Material Design 3\n" +
                    "• MPAndroidChart\n" +
                    "• WorkManager\n\n" +
                    "No internet required. Your data stays on your device."
                )
                .setPositiveButton("OK", null)
                .show()
        }

        binding.rowClearAlerts.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear Acknowledged Alerts")
                .setMessage("Remove all dismissed restock alerts from history?")
                .setPositiveButton("Clear") { _, _ ->
                    GlobalScope.launch(Dispatchers.IO) {
                        app.alertRepository.clearAcknowledgedAlerts()
                        runOnUiThread { toast("Alerts cleared") }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showEditShopNameDialog() {
        val input = com.google.android.material.textfield.TextInputEditText(this)
        input.setText(app.userRepository.getShopName())
        input.hint = "Shop Name"

        AlertDialog.Builder(this)
            .setTitle("Edit Shop Name")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val name = input.text?.toString()?.trim()
                if (!name.isNullOrEmpty()) {
                    app.userRepository.setShopName(name)
                    binding.tvShopNameValue.text = name
                    toast("Shop name updated")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
