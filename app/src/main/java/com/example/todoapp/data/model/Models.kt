package com.example.todoapp.data.model

data class User(
    val userId: Int = 0,
    val username: String,
    val email: String,
    val fullName: String = "",
    val phone: String = ""
)

data class Category(
    val categoryId: Int = 0,
    val name: String,
    val icon: String = "ic_folder",
    val color: String = "#2196F3",
    val userId: Int
)

data class Task(
    val taskId: Int = 0,
    val title: String,
    val dueDate: String? = null,
    val note: String? = null,
    val reminderTime: String? = null,
    val isCompleted: Boolean = false,
    val userId: Int,
    val categoryId: Int,
    // joined fields
    val categoryName: String = "",
    val categoryIcon: String = "",
    val categoryColor: String = "",
    val tags: List<Tag> = emptyList()
)

data class Tag(
    val tagId: Int = 0,
    val name: String,
    val color: String = "#FF9800",
    val userId: Int
)

data class TagTask(
    val tagId: Int,
    val taskId: Int,
    val userId: Int
)

data class StatsSummary(
    val total: Int,
    val completed: Int,
    val pending: Int,
    val completionRate: Float
)
