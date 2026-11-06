package com.example.todoapp.ui.tag

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.data.model.Tag
import com.example.todoapp.data.repository.TagRepository
import com.example.todoapp.utils.SessionManager
import com.example.todoapp.utils.toast
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TagFragment : Fragment() {

    private lateinit var rvTags: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var tagRepo: TagRepository
    private lateinit var session: SessionManager
    private lateinit var adapter: TagAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tag, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tagRepo = TagRepository(requireContext())
        session = SessionManager(requireContext())

        rvTags = view.findViewById(R.id.rvTags)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        fabAdd = view.findViewById(R.id.fabAdd)

        adapter = TagAdapter(
            onEdit = { tag -> showEditDialog(tag) },
            onDelete = { tag -> confirmDelete(tag) }
        )
        rvTags.layoutManager = LinearLayoutManager(requireContext())
        rvTags.adapter = adapter

        fabAdd.setOnClickListener { showAddDialog() }
        loadTags()
    }

    override fun onResume() { super.onResume(); loadTags() }

    private fun loadTags() {
        val tags = tagRepo.getAllByUser(session.getUserId())
        adapter.submitList(tags)
        tvEmpty.visibility = if (tags.isEmpty()) View.VISIBLE else View.GONE
        rvTags.visibility = if (tags.isEmpty()) View.GONE else View.VISIBLE
    }

    private val colorOptions = listOf("#F44336", "#FF9800", "#4CAF50", "#2196F3", "#9C27B0")

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_tag, null)
        val etName = dialogView.findViewById<EditText>(R.id.etTagName)
        var selectedColor = colorOptions[0]
        val colorViews = listOf(dialogView.findViewById<View>(R.id.vColor1), dialogView.findViewById<View>(R.id.vColor2),
            dialogView.findViewById<View>(R.id.vColor3), dialogView.findViewById<View>(R.id.vColor4), dialogView.findViewById<View>(R.id.vColor5))
        colorViews.forEachIndexed { i, v ->
            v.setBackgroundColor(Color.parseColor(colorOptions[i]))
            v.setOnClickListener { selectedColor = colorOptions[i] }
        }
        AlertDialog.Builder(requireContext()).setTitle("Thêm nhãn").setView(dialogView)
            .setPositiveButton("Thêm") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isEmpty()) { requireContext().toast("Vui lòng nhập tên nhãn"); return@setPositiveButton }
                val tag = Tag(name = name, color = selectedColor, userId = session.getUserId())
                if (tagRepo.insert(tag) > 0) { requireContext().toast("Thêm thành công!"); loadTags() }
                else requireContext().toast("Thêm thất bại!")
            }.setNegativeButton("Hủy", null).show()
    }

    private fun showEditDialog(tag: Tag) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_tag, null)
        val etName = dialogView.findViewById<EditText>(R.id.etTagName)
        etName.setText(tag.name)
        var selectedColor = tag.color
        val colorViews = listOf(dialogView.findViewById<View>(R.id.vColor1), dialogView.findViewById<View>(R.id.vColor2),
            dialogView.findViewById<View>(R.id.vColor3), dialogView.findViewById<View>(R.id.vColor4), dialogView.findViewById<View>(R.id.vColor5))
        colorViews.forEachIndexed { i, v ->
            v.setBackgroundColor(Color.parseColor(colorOptions[i]))
            v.setOnClickListener { selectedColor = colorOptions[i] }
        }
        AlertDialog.Builder(requireContext()).setTitle("Sửa nhãn").setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isEmpty()) { requireContext().toast("Vui lòng nhập tên nhãn"); return@setPositiveButton }
                tagRepo.update(tag.copy(name = name, color = selectedColor))
                requireContext().toast("Cập nhật thành công!")
                loadTags()
            }.setNegativeButton("Hủy", null).show()
    }

    private fun confirmDelete(tag: Tag) {
        AlertDialog.Builder(requireContext()).setTitle("Xóa nhãn")
            .setMessage("Bạn có chắc muốn xóa nhãn '${tag.name}'?")
            .setPositiveButton("Xóa") { _, _ ->
                tagRepo.delete(tag.tagId, session.getUserId())
                requireContext().toast("Đã xóa nhãn")
                loadTags()
            }.setNegativeButton("Hủy", null).show()
    }
}

class TagAdapter(
    private val onEdit: (Tag) -> Unit,
    private val onDelete: (Tag) -> Unit
) : ListAdapter<Tag, TagAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Tag>() {
            override fun areItemsTheSame(o: Tag, n: Tag) = o.tagId == n.tagId
            override fun areContentsTheSame(o: Tag, n: Tag) = o == n
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val vColor: View = view.findViewById(R.id.vTagColor)
        val tvName: TextView = view.findViewById(R.id.tvTagName)
        val ibEdit: android.widget.ImageButton = view.findViewById(R.id.ibEdit)
        val ibDelete: android.widget.ImageButton = view.findViewById(R.id.ibDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tag, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tag = getItem(position)
        holder.tvName.text = tag.name
        try { holder.vColor.setBackgroundColor(Color.parseColor(tag.color)) }
        catch (e: Exception) { holder.vColor.setBackgroundColor(Color.GRAY) }
        holder.ibEdit.setOnClickListener { onEdit(tag) }
        holder.ibDelete.setOnClickListener { onDelete(tag) }
    }
}
