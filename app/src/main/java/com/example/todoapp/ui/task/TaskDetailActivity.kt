package com.example.todoapp.ui.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.todoapp.R
import com.example.todoapp.data.model.Task
import com.example.todoapp.data.repository.CategoryRepository
import com.example.todoapp.data.repository.TagRepository
import com.example.todoapp.data.repository.TaskRepository
import com.example.todoapp.utils.SessionManager
import com.example.todoapp.utils.chooseTags
import com.example.todoapp.utils.toast
import com.example.todoapp.utils.toDisplayDateTime
import java.util.*

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var ibBack: ImageButton
    private lateinit var cbDone: CheckBox
    private lateinit var tvTitle: TextView
    private lateinit var tvNote: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvTags: TextView
    private lateinit var tvDueDate: TextView
    private lateinit var tvReminderTime: TextView
    private lateinit var tvDelete: TextView
    private lateinit var ivEditTitle: ImageView
    private lateinit var ivEditNote: ImageView

    private lateinit var taskRepo: TaskRepository
    private lateinit var categoryRepo: CategoryRepository
    private lateinit var tagRepo: TagRepository
    private lateinit var session: SessionManager

    private lateinit var task: Task

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        taskRepo = TaskRepository(this)
        categoryRepo = CategoryRepository(this)
        tagRepo = TagRepository(this)
        session = SessionManager(this)

        ibBack = findViewById(R.id.ibBack)
        cbDone = findViewById(R.id.cbDone)
        tvTitle = findViewById(R.id.tvTitle)
        tvNote = findViewById(R.id.tvNote)
        tvCategory = findViewById(R.id.tvCategory)
        tvTags = findViewById(R.id.tvTags)
        tvDueDate = findViewById(R.id.tvDueDate)
        tvReminderTime = findViewById(R.id.tvReminderTime)
        tvDelete = findViewById(R.id.tvDelete)
        ivEditTitle = findViewById(R.id.ivEditTitle)
        ivEditNote = findViewById(R.id.ivEditNote)

        val taskId = intent.getIntExtra("task_id", -1)
        task = taskRepo.getTaskById(taskId, session.getUserId()) ?: run {
            toast("Công việc không còn tồn tại")
            finish()
            return
        }
        bindTask(task)

        ibBack.setOnClickListener { finish() }
        ivEditTitle.setOnClickListener { editTitle() }
        ivEditNote.setOnClickListener { editNote() }
        tvCategory.setOnClickListener { changeCategory() }
        tvTags.setOnClickListener { changeTags() }
        tvDueDate.setOnClickListener { changeDueDate() }
        tvReminderTime.setOnClickListener { changeReminderTime() }
        tvDelete.setOnClickListener { confirmDelete() }

        cbDone.setOnCheckedChangeListener { _, checked ->
            if (taskRepo.updateCompletionStatus(task.taskId, task.userId, checked) > 0) {
                task = task.copy(isCompleted = checked)
            } else {
                toast("Không thể cập nhật công việc")
                finish()
            }
        }
    }

    private fun bindTask(t: Task) {
        cbDone.isChecked = t.isCompleted
        tvTitle.text = t.title
        tvNote.text = if (t.note.isNullOrEmpty()) "Không có ghi chú" else t.note
        tvCategory.text = t.categoryName.ifEmpty { "Chưa chọn danh mục" }
        tvDueDate.text = t.dueDate?.toDisplayDateTime() ?: "Không có hạn"
        tvReminderTime.text = t.reminderTime?.toDisplayDateTime() ?: "Chưa lưu giờ nhắc"
        tvTags.text = if (t.tags.isEmpty()) "Không có nhãn" else t.tags.joinToString(", ") { it.name }
    }

    private fun save(updated: Task) {
        if (taskRepo.update(updated) > 0) {
            task = updated
            bindTask(task)
        } else {
            toast("Không thể cập nhật công việc")
        }
    }

    private fun editTitle() {
        val et = EditText(this).apply { setText(task.title) }
        AlertDialog.Builder(this).setTitle("Sửa tiêu đề")
            .setView(et)
            .setPositiveButton("Lưu") { _, _ ->
                val newTitle = et.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    save(task.copy(title = newTitle))
                }
            }.setNegativeButton("Hủy", null).show()
    }

    private fun editNote() {
        val et = EditText(this).apply { setText(task.note) }
        AlertDialog.Builder(this).setTitle("Sửa ghi chú")
            .setView(et)
            .setPositiveButton("Lưu") { _, _ ->
                val newNote = et.text.toString().trim()
                save(task.copy(note = newNote))
            }.setNegativeButton("Hủy", null).show()
    }

    private fun changeCategory() {
        val categories = categoryRepo.getAllByUser(session.getUserId())
        val names = categories.map { it.name }.toTypedArray()
        AlertDialog.Builder(this).setTitle("Chọn danh mục")
            .setItems(names) { _, which ->
                val category = categories[which]
                save(task.copy(categoryId = category.categoryId, categoryName = category.name))
            }.show()
    }

    private fun changeTags() {
        val tags = tagRepo.getAllByUser(session.getUserId())
        chooseTags(tags, task.tags, "Chọn nhãn", "Lưu") { chosen ->
            save(task.copy(tags = chosen))
        }
    }

    private fun changeDueDate() {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            TimePickerDialog(this, { _, h, min ->
                val date = String.format("%04d-%02d-%02d %02d:%02d:00", y, m + 1, d, h, min)
                save(task.copy(dueDate = date))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun changeReminderTime() {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            TimePickerDialog(this, { _, h, min ->
                val date = String.format("%04d-%02d-%02d %02d:%02d:00", y, m + 1, d, h, min)
                save(task.copy(reminderTime = date))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Xóa công việc")
            .setMessage("Bạn có chắc muốn xóa công việc này?")
            .setPositiveButton("Xóa") { _, _ ->
                if (taskRepo.delete(task.taskId, task.userId) > 0) {
                    toast("Đã xóa công việc")
                    finish()
                } else {
                    toast("Không thể xóa công việc")
                }
            }.setNegativeButton("Hủy", null).show()
    }
}
