package com.nimbus.app

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.nimbus.app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pre-fill the email if the person just registered, or asked to be remembered.
        val prefillFromRegister = intent.getStringExtra("prefill_email")
        val remembered = UserStore.getRemembered(this)
        when {
            prefillFromRegister != null -> binding.etEmail.setText(prefillFromRegister)
            remembered != null -> {
                binding.etEmail.setText(remembered)
                binding.cbRemember.isChecked = true
            }
        }

        binding.etEmail.doOnTextChanged { _, _, _, _ -> binding.errEmail.visibility = android.view.View.GONE }
        binding.etPassword.doOnTextChanged { _, _, _, _ -> binding.errPassword.visibility = android.view.View.GONE }

        binding.btnTogglePassword.setOnClickListener {
            passwordVisible = togglePasswordVisibility(binding.etPassword, binding.btnTogglePassword, passwordVisible)
        }

        binding.linkRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.linkForgotPassword.setOnClickListener {
            Toast.makeText(this, getString(R.string.msg_forgot_password), Toast.LENGTH_LONG).show()
        }

        binding.btnLogin.setOnClickListener { onLoginClicked() }
    }

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

    private fun onLoginClicked() {
        hideBanner()

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        var isValid = true
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.errEmail.text = getString(R.string.err_email_invalid)
            binding.errEmail.visibility = android.view.View.VISIBLE
            isValid = false
        }
        if (password.length < 8) {
            binding.errPassword.text = getString(R.string.err_password_short)
            binding.errPassword.visibility = android.view.View.VISIBLE
            isValid = false
        }
        if (!isValid) return

        val user = UserStore.getUser(this, email)
        if (user == null || user.password != password) {
            showBanner(getString(R.string.err_invalid_login))
            return
        }

        UserStore.setRemembered(this, if (binding.cbRemember.isChecked) email else null)

        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("name", user.name)
        intent.putExtra("email", user.email)
        startActivity(intent)
        finish()
    }

    private fun showBanner(message: String) {
        binding.loginBanner.text = message
        binding.loginBanner.visibility = android.view.View.VISIBLE
        (binding.loginBanner.background as GradientDrawable).setColor(ContextCompat.getColor(this, R.color.danger_bg))
        binding.loginBanner.setTextColor(ContextCompat.getColor(this, R.color.danger))
    }

    private fun hideBanner() {
        binding.loginBanner.visibility = android.view.View.GONE
    }
}
