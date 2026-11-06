package com.example.todoapp.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.data.model.Category
import com.example.todoapp.data.repository.CategoryRepository
import com.example.todoapp.utils.SessionManager
import com.example.todoapp.utils.toast
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CategoryFragment : Fragment() {

    private lateinit var rvCategories: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var fabAdd: FloatingActionButton

    private lateinit var categoryRepo: CategoryRepository
    private lateinit var session: SessionManager
    private lateinit var adapter: CategoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoryRepo = CategoryRepository(requireContext())
        session = SessionManager(requireContext())

        rvCategories = view.findViewById(R.id.rvCategories)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        fabAdd = view.findViewById(R.id.fabAdd)

        adapter = CategoryAdapter(
            onEdit = { cat -> showEditDialog(cat) },
            onDelete = { cat -> confirmDelete(cat) }
        )
        rvCategories.layoutManager = LinearLayoutManager(requireContext())
        rvCategories.adapter = adapter

        fabAdd.setOnClickListener { showAddDialog() }
        loadCategories()
    }

    override fun onResume() {
        super.onResume()
        loadCategories()
    }

    private fun loadCategories() {
        val cats = categoryRepo.getAllByUser(session.getUserId())
        adapter.submitList(cats)
        tvEmpty.visibility = if (cats.isEmpty()) View.VISIBLE else View.GONE
        rvCategories.visibility = if (cats.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_category, null)
        val etName = dialogView.findViewById<EditText>(R.id.etCategoryName)
        val colorOptions = listOf("#2196F3", "#4CAF50", "#F44336", "#FF9800", "#9C27B0")
        var selectedColor = colorOptions[0]
        val colorViews = listOf(
            dialogView.findViewById<View>(R.id.vColor1),
            dialogView.findViewById<View>(R.id.vColor2),
            dialogView.findViewById<View>(R.id.vColor3),
            dialogView.findViewById<View>(R.id.vColor4),
            dialogView.findViewById<View>(R.id.vColor5)
        )
        colorViews.forEachIndexed { i, v ->
            v.setBackgroundColor(android.graphics.Color.parseColor(colorOptions[i]))
            v.setOnClickListener { selectedColor = colorOptions[i] }
        }
        val iconOptions = listOf("ic_folder", "ic_work", "ic_school", "ic_home", "ic_star")
        var selectedIcon = iconOptions[0]

        AlertDialog.Builder(requireContext())
            .setTitle("Thêm danh mục")
            .setView(dialogView)
            .setPositiveButton("Thêm") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isEmpty()) { requireContext().toast("Vui lòng nhập tên danh mục"); return@setPositiveButton }
                val cat = Category(name = name, icon = selectedIcon, color = selectedColor, userId = session.getUserId())
                val result = categoryRepo.insert(cat)
                if (result > 0) { requireContext().toast("Thêm thành công!"); loadCategories() }
                else requireContext().toast("Thêm thất bại!")
            }.setNegativeButton("Hủy", null).show()
    }

    private fun showEditDialog(category: Category) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_category, null)
        val etName = dialogView.findViewById<EditText>(R.id.etCategoryName)
        etName.setText(category.name)
        val colorOptions = listOf("#2196F3", "#4CAF50", "#F44336", "#FF9800", "#9C27B0")
        var selectedColor = category.color
        val colorViews = listOf(
            dialogView.findViewById<View>(R.id.vColor1),
            dialogView.findViewById<View>(R.id.vColor2),
            dialogView.findViewById<View>(R.id.vColor3),
            dialogView.findViewById<View>(R.id.vColor4),
            dialogView.findViewById<View>(R.id.vColor5)
        )
        colorViews.forEachIndexed { i, v ->
            v.setBackgroundColor(android.graphics.Color.parseColor(colorOptions[i]))
            v.setOnClickListener { selectedColor = colorOptions[i] }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Sửa danh mục")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isEmpty()) { requireContext().toast("Vui lòng nhập tên danh mục"); return@setPositiveButton }
                val updated = category.copy(name = name, color = selectedColor)
                categoryRepo.update(updated)
                requireContext().toast("Cập nhật thành công!")
                loadCategories()
            }.setNegativeButton("Hủy", null).show()
    }

    private fun confirmDelete(category: Category) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa danh mục")
            .setMessage("Chỉ có thể xóa danh mục '${category.name}' khi danh mục không còn công việc.")
            .setPositiveButton("Xóa") { _, _ ->
                if (categoryRepo.delete(category.categoryId, session.getUserId()) > 0) {
                    requireContext().toast("Đã xóa danh mục")
                } else {
                    requireContext().toast("Không thể xóa. Hãy chuyển các công việc sang danh mục khác trước.")
                }
                loadCategories()
            }.setNegativeButton("Hủy", null).show()
    }
}
