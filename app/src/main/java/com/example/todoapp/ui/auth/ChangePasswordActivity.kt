package com.example.todoapp.ui.auth

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.todoapp.R
import com.example.todoapp.data.repository.UserRepository
import com.example.todoapp.utils.SessionManager
import com.example.todoapp.utils.toast

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var ibBack: ImageButton
    private lateinit var etOldPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnChange: Button

    private lateinit var userRepo: UserRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        userRepo = UserRepository(this)
        session = SessionManager(this)

        ibBack = findViewById(R.id.ibBack)
        etOldPassword = findViewById(R.id.etOldPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnChange = findViewById(R.id.btnChange)

        ibBack.setOnClickListener { finish() }
        btnChange.setOnClickListener { doChange() }
    }

    private fun doChange() {
        val oldPassword = etOldPassword.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (oldPassword.isEmpty() || newPassword.isEmpty()) { toast("Vui lòng điền đầy đủ thông tin"); return }
        if (newPassword != confirmPassword) { toast("Mật khẩu xác nhận không khớp"); return }
        if (newPassword.length < 6) { toast("Mật khẩu phải có ít nhất 6 ký tự"); return }

        val userId = session.getUserId()
        runAccountAction(btnChange, { userRepo.changePassword(userId, oldPassword, newPassword) }) { changed ->
            if (changed) {
                toast("Đổi mật khẩu thành công!")
                finish()
            } else {
                toast("Mật khẩu cũ không đúng")
            }
        }
    }
}
