package com.example.todoapp.ui.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.todoapp.R
import com.example.todoapp.data.model.Category
import com.example.todoapp.data.model.Tag
import com.example.todoapp.data.model.Task
import com.example.todoapp.data.repository.CategoryRepository
import com.example.todoapp.data.repository.TagRepository
import com.example.todoapp.data.repository.TaskRepository
import com.example.todoapp.utils.SessionManager
import com.example.todoapp.utils.chooseTags
import com.example.todoapp.utils.toast
import java.util.*

class AddTaskActivity : AppCompatActivity() {

    private lateinit var ibBack: ImageButton
    private lateinit var etTitle: EditText
    private lateinit var etNote: EditText
    private lateinit var tvCategory: TextView
    private lateinit var tvTags: TextView
    private lateinit var tvDueDate: TextView
    private lateinit var tvReminderTime: TextView
    private lateinit var fabSave: com.google.android.material.floatingactionbutton.FloatingActionButton

    private lateinit var taskRepo: TaskRepository
    private lateinit var categoryRepo: CategoryRepository
    private lateinit var tagRepo: TagRepository
    private lateinit var session: SessionManager

    private var selectedCategory: Category? = null
    private var selectedTags = emptyList<Tag>()
    private var dueDate: String? = null
    private var reminderTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        taskRepo = TaskRepository(this)
        categoryRepo = CategoryRepository(this)
        tagRepo = TagRepository(this)
        session = SessionManager(this)

        ibBack = findViewById(R.id.ibBack)
        etTitle = findViewById(R.id.etTitle)
        etNote = findViewById(R.id.etNote)
        tvCategory = findViewById(R.id.tvCategory)
        tvTags = findViewById(R.id.tvTags)
        tvDueDate = findViewById(R.id.tvDueDate)
        tvReminderTime = findViewById(R.id.tvReminderTime)
        fabSave = findViewById(R.id.fabSave)

        ibBack.setOnClickListener { finish() }

        tvCategory.setOnClickListener { showCategoryDialog() }
        tvTags.setOnClickListener { showTagsDialog() }
        tvDueDate.setOnClickListener { showDateTimePicker { date -> dueDate = date; tvDueDate.text = date } }
        tvReminderTime.setOnClickListener { showDateTimePicker { date -> reminderTime = date; tvReminderTime.text = date } }

        fabSave.setOnClickListener { saveTask() }
    }

    private fun showCategoryDialog() {
        val userId = session.getUserId()
        val categories = categoryRepo.getAllByUser(userId)
        if (categories.isEmpty()) { toast("Chưa có danh mục nào. Vui lòng tạo danh mục trước!"); return }
        val names = categories.map { it.name }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Chọn danh mục")
            .setItems(names) { _, which ->
                selectedCategory = categories[which]
                tvCategory.text = categories[which].name
            }.show()
    }

    private fun showTagsDialog() {
        val tags = tagRepo.getAllByUser(session.getUserId())
        chooseTags(tags, selectedTags, "Chọn nhãn", "Lưu") { chosen ->
            selectedTags = chosen
            tvTags.text = if (chosen.isEmpty()) "Chọn nhãn" else chosen.joinToString(", ") { it.name }
        }
    }

    private fun showDateTimePicker(onResult: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            TimePickerDialog(this, { _, h, min ->
                val date = String.format("%04d-%02d-%02d %02d:%02d:00", y, m + 1, d, h, min)
                onResult(date)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveTask() {
        val title = etTitle.text.toString().trim()
        val note = etNote.text.toString().trim()

        if (title.isEmpty()) { toast("Vui lòng nhập tiêu đề công việc"); return }
        val category = selectedCategory ?: run {
            toast("Vui lòng chọn danh mục")
            return
        }

        val task = Task(
            title = title,
            note = if (note.isEmpty()) null else note,
            dueDate = dueDate,
            reminderTime = reminderTime,
            userId = session.getUserId(),
            categoryId = category.categoryId,
            tags = selectedTags
        )

        val result = taskRepo.insert(task)
        if (result > 0) {
            toast("Thêm công việc thành công!")
            finish()
        } else {
            toast("Thêm công việc thất bại!")
        }
    }
}
