package com.example.todoapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.todoapp.R
import com.example.todoapp.data.repository.UserRepository
import com.example.todoapp.ui.auth.ChangePasswordActivity
import com.example.todoapp.ui.auth.LoginActivity
import com.example.todoapp.ui.tag.TagFragment
import com.example.todoapp.utils.SessionManager
import com.example.todoapp.utils.isValidEmail
import com.example.todoapp.utils.toast

class ProfileFragment : Fragment() {

    private lateinit var tvUsername: TextView
    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var btnChangePassword: Button
    private lateinit var btnManageTags: Button
    private lateinit var btnLogout: Button

    private lateinit var userRepo: UserRepository
    private lateinit var session: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userRepo = UserRepository(requireContext())
        session = SessionManager(requireContext())

        tvUsername = view.findViewById(R.id.tvUsername)
        tvFullName = view.findViewById(R.id.tvFullName)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvPhone = view.findViewById(R.id.tvPhone)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnChangePassword = view.findViewById(R.id.btnChangePassword)
        btnManageTags = view.findViewById(R.id.btnManageTags)
        btnLogout = view.findViewById(R.id.btnLogout)

        loadProfile()

        btnEditProfile.setOnClickListener { showEditDialog() }
        btnChangePassword.setOnClickListener {
            startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
        }
        btnManageTags.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, TagFragment())
                .addToBackStack(null)
                .commit()
        }
        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất?")
                .setPositiveButton("Đăng xuất") { _, _ ->
                    session.clearSession()
                    startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }.setNegativeButton("Hủy", null).show()
        }
    }

    override fun onResume() { super.onResume(); loadProfile() }

    private fun loadProfile() {
        val user = userRepo.getUserById(session.getUserId()) ?: return
        tvUsername.text = user.username
        tvFullName.text = user.fullName.ifEmpty { "Chưa cập nhật" }
        tvEmail.text = user.email
        tvPhone.text = user.phone.ifEmpty { "Chưa cập nhật" }
    }

    private fun showEditDialog() {
        val user = userRepo.getUserById(session.getUserId()) ?: return
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etFullName = dialogView.findViewById<EditText>(R.id.etFullName)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
        val etPhone = dialogView.findViewById<EditText>(R.id.etPhone)
        etFullName.setText(user.fullName)
        etEmail.setText(user.email)
        etPhone.setText(user.phone)

        AlertDialog.Builder(requireContext()).setTitle("Chỉnh sửa thông tin")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val email = etEmail.text.toString().trim()
                if (email.isNotEmpty() && !email.isValidEmail()) {
                    requireContext().toast("Email không hợp lệ"); return@setPositiveButton
                }
                val updated = user.copy(
                    fullName = etFullName.text.toString().trim(),
                    email = email.ifEmpty { user.email },
                    phone = etPhone.text.toString().trim()
                )
                userRepo.updateUser(updated)
                requireContext().toast("Cập nhật thành công!")
                loadProfile()
            }.setNegativeButton("Hủy", null).show()
    }
}
