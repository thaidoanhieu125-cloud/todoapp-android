package com.example.todoapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.todoapp.R
import com.example.todoapp.data.repository.UserRepository
import com.example.todoapp.ui.main.MainActivity
import com.example.todoapp.utils.SessionManager
import com.example.todoapp.utils.toast

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView

    private lateinit var userRepo: UserRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userRepo = UserRepository(this)
        session = SessionManager(this)

        if (session.isLoggedIn()) {
            goToMain()
            return
        }

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)

        btnLogin.setOnClickListener { doLogin() }
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun doLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            toast("Vui lòng điền đầy đủ thông tin")
            return
        }

        runAccountAction(btnLogin, { userRepo.login(username, password) }) { user ->
            if (user != null) {
                session.saveSession(user.userId, user.username)
                goToMain()
            } else {
                toast("Tên đăng nhập hoặc mật khẩu không đúng")
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
