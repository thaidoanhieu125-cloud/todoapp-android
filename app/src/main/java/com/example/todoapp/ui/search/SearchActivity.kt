package com.example.todoapp.ui.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.data.model.Tag
import com.example.todoapp.data.repository.TagRepository
import com.example.todoapp.data.repository.TaskRepository
import com.example.todoapp.ui.task.TaskAdapter
import com.example.todoapp.ui.task.TaskDetailActivity
import com.example.todoapp.utils.SessionManager
import com.example.todoapp.utils.chooseTags

class SearchActivity : AppCompatActivity() {

    private lateinit var ibBack: ImageButton
    private lateinit var etSearch: EditText
    private lateinit var rvTasks: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var llSelectedTags: LinearLayout
    private lateinit var btnFilterTags: Button

    private lateinit var taskRepo: TaskRepository
    private lateinit var tagRepo: TagRepository
    private lateinit var session: SessionManager
    private lateinit var adapter: TaskAdapter

    private var selectedTags = emptyList<Tag>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        taskRepo = TaskRepository(this)
        tagRepo = TagRepository(this)
        session = SessionManager(this)

        ibBack = findViewById(R.id.ibBack)
        etSearch = findViewById(R.id.etSearch)
        rvTasks = findViewById(R.id.rvTasks)
        tvEmpty = findViewById(R.id.tvEmpty)
        llSelectedTags = findViewById(R.id.llSelectedTags)
        btnFilterTags = findViewById(R.id.btnFilterTags)

        adapter = TaskAdapter(
            onCheck = { task, checked ->
                taskRepo.updateCompletionStatus(task.taskId, task.userId, checked)
                doSearch()
            },
            onClick = { task ->
                val intent = Intent(this, TaskDetailActivity::class.java)
                intent.putExtra("task_id", task.taskId)
                startActivity(intent)
            }
        )
        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter = adapter

        ibBack.setOnClickListener { finish() }
        btnFilterTags.setOnClickListener { showTagFilterDialog() }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { doSearch() }
            override fun afterTextChanged(s: Editable?) {}
        })

    }

    override fun onResume() {
        super.onResume()
        doSearch()
    }

    private fun doSearch() {
        val keyword = etSearch.text.toString().trim()
        val tasks = taskRepo.search(session.getUserId(), keyword, selectedTags.map { it.tagId })
        adapter.submitList(tasks)
        tvEmpty.visibility = if (tasks.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        rvTasks.visibility = if (tasks.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun showTagFilterDialog() {
        val tags = tagRepo.getAllByUser(session.getUserId())
        chooseTags(tags, selectedTags, "Lọc theo nhãn", "Lọc") { chosen ->
            selectedTags = chosen
            updateTagChips()
            doSearch()
        }
    }

    private fun updateTagChips() {
        llSelectedTags.removeAllViews()
        selectedTags.forEach { tag ->
            val chip = TextView(this).apply {
                text = tag.name
                textSize = 12f
                setPadding(16, 8, 16, 8)
                setTextColor(android.graphics.Color.WHITE)
                setBackgroundColor(android.graphics.Color.parseColor("#2196F3"))
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 8, 0)
                layoutParams = params
            }
            llSelectedTags.addView(chip)
        }
    }
}
