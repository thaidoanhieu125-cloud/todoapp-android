package com.example.todoapp.ui.auth

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.todoapp.R
import com.example.todoapp.data.model.User
import com.example.todoapp.data.repository.UserRepository
import com.example.todoapp.utils.isValidEmail
import com.example.todoapp.utils.toast

class RegisterActivity : AppCompatActivity() {

    private lateinit var ibBack: ImageButton
    private lateinit var etFullName: EditText
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button

    private lateinit var userRepo: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        userRepo = UserRepository(this)

        ibBack = findViewById(R.id.ibBack)
        etFullName = findViewById(R.id.etFullName)
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)

        ibBack.setOnClickListener { finish() }
        btnRegister.setOnClickListener { doRegister() }
    }

    private fun doRegister() {
        val fullName = etFullName.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            toast("Vui lòng điền đầy đủ thông tin bắt buộc"); return
        }
        if (!email.isValidEmail()) { toast("Email không hợp lệ"); return }
        if (password != confirmPassword) { toast("Mật khẩu xác nhận không khớp"); return }
        if (password.length < 6) { toast("Mật khẩu phải có ít nhất 6 ký tự"); return }
        val user = User(username = username, email = email, fullName = fullName, phone = phone)
        runAccountAction(btnRegister, {
            when {
                userRepo.usernameExists(username) -> "Tên đăng nhập đã tồn tại"
                userRepo.emailExists(email) -> "Email đã được sử dụng"
                userRepo.register(user, password) <= 0 -> "Đăng ký thất bại, thử lại!"
                else -> null
            }
        }) { error ->
            if (error != null) {
                toast(error)
            } else {
                toast("Đăng ký thành công!")
                finish()
            }
        }
    }
}
