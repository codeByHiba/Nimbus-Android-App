package com.nimbus.app

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Patterns
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.nimbus.app.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    // Track show/hide state for each password field independently.
    private var passwordVisible = false
    private var confirmVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Clear an error as soon as the person starts fixing that field.
        binding.etName.doOnTextChanged { _, _, _, _ -> binding.errName.visibility = android.view.View.GONE }
        binding.etEmail.doOnTextChanged { _, _, _, _ -> binding.errEmail.visibility = android.view.View.GONE }
        binding.etPhone.doOnTextChanged { _, _, _, _ -> binding.errPhone.visibility = android.view.View.GONE }
        binding.etPassword.doOnTextChanged { _, _, _, _ -> binding.errPassword.visibility = android.view.View.GONE }
        binding.etConfirmPassword.doOnTextChanged { _, _, _, _ -> binding.errConfirmPassword.visibility = android.view.View.GONE }

        binding.btnTogglePassword.setOnClickListener {
            passwordVisible = togglePasswordVisibility(binding.etPassword, binding.btnTogglePassword, passwordVisible)
        }
        binding.btnToggleConfirmPassword.setOnClickListener {
            confirmVisible = togglePasswordVisibility(binding.etConfirmPassword, binding.btnToggleConfirmPassword, confirmVisible)
        }

        binding.linkLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnCreateAccount.setOnClickListener { onCreateAccountClicked() }
    }

    /** Swaps an EditText's input type between hidden/visible and updates the eye icon. */
    private fun togglePasswordVisibility(editText: EditText, button: ImageButton, currentlyVisible: Boolean): Boolean {
        val nowVisible = !currentlyVisible
        editText.inputType = if (nowVisible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        editText.setSelection(editText.text.length)
        button.setImageResource(if (nowVisible) R.drawable.ic_eye_closed else R.drawable.ic_eye_open)
        return nowVisible
    }

    private fun onCreateAccountClicked() {
        hideBanner()

        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        var isValid = true

        if (name.length < 2) {
            showFieldError(binding.errName, getString(R.string.err_name_required))
            isValid = false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showFieldError(binding.errEmail, getString(R.string.err_email_invalid))
            isValid = false
        }
        // Accepts an optional "+" followed by 7-15 digits (spaces/dashes are stripped first).
        val cleanedPhone = phone.replace(" ", "").replace("-", "")
        if (!cleanedPhone.matches(Regex("^\\+?[0-9]{7,15}$"))) {
            showFieldError(binding.errPhone, getString(R.string.err_phone_invalid))
            isValid = false
        }
        if (password.length < 8) {
            showFieldError(binding.errPassword, getString(R.string.err_password_short))
            isValid = false
        }
        if (confirmPassword != password || confirmPassword.isEmpty()) {
            showFieldError(binding.errConfirmPassword, getString(R.string.err_password_mismatch))
            isValid = false
        }

        if (!isValid) return

        if (UserStore.userExists(this, email)) {
            showBanner(getString(R.string.err_account_exists), isSuccess = false)
            return
        }

        UserStore.saveUser(this, NimbusUser(name = name, email = email, phone = cleanedPhone, password = password))
        showBanner(getString(R.string.success_account_created), isSuccess = true)

        // Give the person a moment to read the success message, then hand off to Login,
        // pre-filling the email they just registered with.
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("prefill_email", email)
            startActivity(intent)
            finish()
        }, 900)
    }

    private fun showFieldError(errorView: android.widget.TextView, message: String) {
        errorView.text = message
        errorView.visibility = android.view.View.VISIBLE
    }

    private fun showBanner(message: String, isSuccess: Boolean) {
        binding.regBanner.text = message
        binding.regBanner.visibility = android.view.View.VISIBLE
        val colorRes = if (isSuccess) R.color.ok_bg else R.color.danger_bg
        val textColorRes = if (isSuccess) R.color.ok else R.color.danger
        (binding.regBanner.background as GradientDrawable).setColor(ContextCompat.getColor(this, colorRes))
        binding.regBanner.setTextColor(ContextCompat.getColor(this, textColorRes))
    }

    private fun hideBanner() {
        binding.regBanner.visibility = android.view.View.GONE
    }
}
