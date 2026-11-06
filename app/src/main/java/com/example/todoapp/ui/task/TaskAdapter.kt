package com.example.todoapp.ui.task

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.data.model.Task
import com.example.todoapp.utils.toDisplayDate
import com.example.todoapp.utils.hexToColor

class TaskAdapter(
    private val onCheck: (Task, Boolean) -> Unit,
    private val onClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(o: Task, n: Task) = o.taskId == n.taskId
            override fun areContentsTheSame(o: Task, n: Task) = o == n
        }
    }

    inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cbDone: CheckBox = view.findViewById(R.id.cbTaskDone)
        val tvTitle: TextView = view.findViewById(R.id.tvTaskTitle)
        val tvDueDate: TextView = view.findViewById(R.id.tvTaskDueDate)
        val tvCategory: TextView = view.findViewById(R.id.tvTaskCategory)
        val llTags: LinearLayout = view.findViewById(R.id.llTags)
        val vCategoryColor: View = view.findViewById(R.id.vCategoryColor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.cbDone.setOnCheckedChangeListener(null)
        holder.cbDone.isChecked = task.isCompleted
        holder.tvTitle.text = task.title
        holder.tvTitle.paintFlags = if (task.isCompleted)
            holder.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        else
            holder.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

        holder.tvDueDate.text = task.dueDate?.toDisplayDate() ?: "Không có hạn"
        holder.tvCategory.text = task.categoryName

        try {
            holder.vCategoryColor.setBackgroundColor(Color.parseColor(task.categoryColor))
        } catch (e: Exception) {
            holder.vCategoryColor.setBackgroundColor(Color.BLUE)
        }

        // Tags chips
        holder.llTags.removeAllViews()
        task.tags.take(3).forEach { tag ->
            val chip = TextView(holder.itemView.context).apply {
                text = tag.name
                textSize = 10f
                setPadding(12, 4, 12, 4)
                setTextColor(Color.WHITE)
                try { setBackgroundColor(Color.parseColor(tag.color)) } catch (e: Exception) { setBackgroundColor(Color.GRAY) }
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 8, 0)
                layoutParams = params
            }
            holder.llTags.addView(chip)
        }

        holder.cbDone.setOnCheckedChangeListener { _, checked ->
            onCheck(task, checked)
        }
        holder.itemView.setOnClickListener { onClick(task) }
    }
}
