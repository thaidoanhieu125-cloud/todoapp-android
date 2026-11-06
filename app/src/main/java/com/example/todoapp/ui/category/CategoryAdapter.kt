package com.example.todoapp.ui.category

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.data.model.Category

class CategoryAdapter(
    private val onEdit: (Category) -> Unit,
    private val onDelete: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Category>() {
            override fun areItemsTheSame(o: Category, n: Category) = o.categoryId == n.categoryId
            override fun areContentsTheSame(o: Category, n: Category) = o == n
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val vColor: View = view.findViewById(R.id.vColor)
        val tvName: TextView = view.findViewById(R.id.tvCategoryName)
        val ibEdit: ImageButton = view.findViewById(R.id.ibEdit)
        val ibDelete: ImageButton = view.findViewById(R.id.ibDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cat = getItem(position)
        holder.tvName.text = cat.name
        try { holder.vColor.setBackgroundColor(Color.parseColor(cat.color)) }
        catch (e: Exception) { holder.vColor.setBackgroundColor(Color.BLUE) }
        holder.ibEdit.setOnClickListener { onEdit(cat) }
        holder.ibDelete.setOnClickListener { onDelete(cat) }
    }
}
