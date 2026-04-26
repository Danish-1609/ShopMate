package com.shopmate.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.shopmate.ShopMateApp
import com.shopmate.databinding.ActivityAuthBinding
import com.shopmate.ui.MainActivity
import com.shopmate.utils.gone
import com.shopmate.utils.toast
import com.shopmate.utils.visible
import com.shopmate.viewmodels.AuthState
import com.shopmate.viewmodels.AuthViewModel
import com.shopmate.viewmodels.ViewModelFactory

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val app by lazy { application as ShopMateApp }

    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactory(app.productRepository, app.saleRepository, app.userRepository, app.alertRepository)
    }

    private var isSetupMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupClickListeners()
        viewModel.checkInitialState()
    }

    private fun setupObservers() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                AuthState.AUTHENTICATED -> goToMain()
                AuthState.SETUP -> setSetupMode(true)
                AuthState.LOGIN -> setSetupMode(false)
                AuthState.LOGIN_FAILED -> {
                    toast("Invalid username or password")
                    binding.tilPassword.error = "Incorrect password"
                }
                AuthState.SETUP_FAILED -> toast("Failed to create account. Try again.")
            }
        }

        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnPrimary.isEnabled = !loading
        }
    }

    private fun setupClickListeners() {
        binding.btnPrimary.setOnClickListener {
            clearErrors()
            if (isSetupMode) attemptSetup() else attemptLogin()
        }

        binding.btnToggleMode.setOnClickListener {
            setSetupMode(!isSetupMode)
        }

        binding.btnSkipLogin.setOnClickListener {
            val shopName = binding.etShopName.text?.toString()?.trim()
                .takeIf { !it.isNullOrEmpty() } ?: "My Shop"
            viewModel.skipLogin(shopName)
        }
    }

    private fun attemptLogin() {
        val username = binding.etUsername.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString() ?: ""

        if (username.isEmpty()) {
            binding.tilUsername.error = "Username is required"
            return
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return
        }
        viewModel.login(username, password)
    }

    private fun attemptSetup() {
        val shopName = binding.etShopName.text?.toString()?.trim() ?: ""
        val username = binding.etUsername.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString() ?: ""

        if (shopName.isEmpty()) {
            binding.tilShopName.error = "Shop name is required"
            return
        }
        if (username.isEmpty()) {
            binding.tilUsername.error = "Username is required"
            return
        }
        if (password.length < 4) {
            binding.tilPassword.error = "Password must be at least 4 characters"
            return
        }
        viewModel.setupAccount(username, password, shopName)
    }

    private fun setSetupMode(setup: Boolean) {
        isSetupMode = setup
        if (setup) {
            binding.tvAuthTitle.text = "Set Up Your Shop"
            binding.tilShopName.visible()
            binding.btnPrimary.text = "Create Account"
            binding.btnToggleMode.text = "Already have an account? Login"
        } else {
            binding.tvAuthTitle.text = "Welcome Back"
            binding.tilShopName.gone()
            binding.btnPrimary.text = "Login"
            binding.btnToggleMode.text = "New? Create Account"
        }
        clearErrors()
    }

    private fun clearErrors() {
        binding.tilShopName.error = null
        binding.tilUsername.error = null
        binding.tilPassword.error = null
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
