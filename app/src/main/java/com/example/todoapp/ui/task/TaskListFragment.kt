package com.example.todoapp.ui.task

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.data.model.Task
import com.example.todoapp.data.repository.TaskRepository
import com.example.todoapp.ui.search.SearchActivity
import com.example.todoapp.utils.SessionManager

class TaskListFragment : Fragment() {

    private lateinit var rvTasks: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var ivSearch: ImageView

    private lateinit var taskRepo: TaskRepository
    private lateinit var session: SessionManager
    private lateinit var adapter: TaskAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_task_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        taskRepo = TaskRepository(requireContext())
        session = SessionManager(requireContext())

        rvTasks = view.findViewById(R.id.rvTasks)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        ivSearch = view.findViewById(R.id.ivSearch)

        adapter = TaskAdapter(
            onCheck = { task, checked -> onTaskChecked(task, checked) },
            onClick = { task -> openTaskDetail(task) }
        )
        rvTasks.layoutManager = LinearLayoutManager(requireContext())
        rvTasks.adapter = adapter

        ivSearch.setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
        }

        loadTasks()
    }

    override fun onResume() {
        super.onResume()
        loadTasks()
    }

    private fun loadTasks() {
        val tasks = taskRepo.getAllByUser(session.getUserId())
        adapter.submitList(tasks)
        tvEmpty.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        rvTasks.visibility = if (tasks.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun onTaskChecked(task: Task, checked: Boolean) {
        taskRepo.updateCompletionStatus(task.taskId, task.userId, checked)
        loadTasks()
    }

    private fun openTaskDetail(task: Task) {
        val intent = Intent(requireContext(), TaskDetailActivity::class.java)
        intent.putExtra("task_id", task.taskId)
        startActivity(intent)
    }
}
