package com.example.todoapp.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.sqlite.transaction
import com.example.todoapp.data.db.DatabaseHelper
import com.example.todoapp.data.model.StatsSummary
import com.example.todoapp.data.model.Task

class TaskRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val tagRepo = TagRepository(context)

    fun insert(task: Task): Long {
        val db = dbHelper.writableDatabase
        val values = taskValues(task).apply {
            put(DatabaseHelper.COL_TASK_USER_ID, task.userId)
        }
        return try {
            db.transaction {
                val taskId = insertOrThrow(DatabaseHelper.TABLE_TASK, null, values)
                insertTagLinks(this, taskId, task)
                taskId
            }
        } catch (_: SQLiteConstraintException) {
            -1L
        }
    }

    fun update(task: Task): Int {
        val db = dbHelper.writableDatabase
        return try {
            db.transaction {
                val rows = update(
                    DatabaseHelper.TABLE_TASK, taskValues(task),
                    "${DatabaseHelper.COL_TASK_ID} = ? AND ${DatabaseHelper.COL_TASK_USER_ID} = ?",
                    arrayOf(task.taskId.toString(), task.userId.toString())
                )
                if (rows > 0) {
                    delete(
                        DatabaseHelper.TABLE_TAGS_TASKS, "${DatabaseHelper.COL_TT_TASK_ID} = ?",
                        arrayOf(task.taskId.toString())
                    )
                    insertTagLinks(this, task.taskId.toLong(), task)
                }
                rows
            }
        } catch (_: SQLiteConstraintException) {
            0
        }
    }

    private fun taskValues(task: Task) = ContentValues().apply {
        put(DatabaseHelper.COL_TASK_TITLE, task.title)
        put(DatabaseHelper.COL_TASK_DUE_DATE, task.dueDate)
        put(DatabaseHelper.COL_TASK_NOTE, task.note)
        put(DatabaseHelper.COL_TASK_REMINDER_TIME, task.reminderTime)
        put(DatabaseHelper.COL_TASK_IS_COMPLETED, if (task.isCompleted) 1 else 0)
        put(DatabaseHelper.COL_TASK_CATEGORY_ID, task.categoryId)
    }

    private fun insertTagLinks(db: SQLiteDatabase, taskId: Long, task: Task) {
        for (tag in task.tags) {
            val values = ContentValues().apply {
                put(DatabaseHelper.COL_TT_TAG_ID, tag.tagId)
                put(DatabaseHelper.COL_TT_TASK_ID, taskId)
                put(DatabaseHelper.COL_TT_USER_ID, task.userId)
            }
            db.insertOrThrow(DatabaseHelper.TABLE_TAGS_TASKS, null, values)
        }
    }

    fun updateCompletionStatus(taskId: Int, userId: Int, isCompleted: Boolean): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_TASK_IS_COMPLETED, if (isCompleted) 1 else 0)
        }
        return db.update(DatabaseHelper.TABLE_TASK, values,
            "${DatabaseHelper.COL_TASK_ID} = ? AND ${DatabaseHelper.COL_TASK_USER_ID} = ?",
            arrayOf(taskId.toString(), userId.toString()))
    }

    fun delete(taskId: Int, userId: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(DatabaseHelper.TABLE_TASK,
            "${DatabaseHelper.COL_TASK_ID} = ? AND ${DatabaseHelper.COL_TASK_USER_ID} = ?",
            arrayOf(taskId.toString(), userId.toString()))
    }

    fun getAllByUser(userId: Int): List<Task> {
        val db = dbHelper.readableDatabase
        val sql = """
            SELECT t.*, c.${DatabaseHelper.COL_CATEGORY_NAME} as cat_name,
                   c.${DatabaseHelper.COL_CATEGORY_ICON} as cat_icon,
                   c.${DatabaseHelper.COL_CATEGORY_COLOR} as cat_color
            FROM ${DatabaseHelper.TABLE_TASK} t
            LEFT JOIN ${DatabaseHelper.TABLE_CATEGORY} c ON t.${DatabaseHelper.COL_TASK_CATEGORY_ID} = c.${DatabaseHelper.COL_CATEGORY_ID}
            WHERE t.${DatabaseHelper.COL_TASK_USER_ID} = ?
            ORDER BY t.${DatabaseHelper.COL_TASK_DUE_DATE} ASC
        """.trimIndent()
        val cursor = db.rawQuery(sql, arrayOf(userId.toString()))
        return cursor.use { it.toList { c -> mapCursorWithTags(c) } }
    }

    fun getByDate(userId: Int, date: String): List<Task> {
        val db = dbHelper.readableDatabase
        val sql = """
            SELECT t.*, c.${DatabaseHelper.COL_CATEGORY_NAME} as cat_name,
                   c.${DatabaseHelper.COL_CATEGORY_ICON} as cat_icon,
                   c.${DatabaseHelper.COL_CATEGORY_COLOR} as cat_color
            FROM ${DatabaseHelper.TABLE_TASK} t
            LEFT JOIN ${DatabaseHelper.TABLE_CATEGORY} c ON t.${DatabaseHelper.COL_TASK_CATEGORY_ID} = c.${DatabaseHelper.COL_CATEGORY_ID}
            WHERE t.${DatabaseHelper.COL_TASK_USER_ID} = ?
              AND DATE(t.${DatabaseHelper.COL_TASK_DUE_DATE}) = ?
            ORDER BY t.${DatabaseHelper.COL_TASK_DUE_DATE} ASC
        """.trimIndent()
        val cursor = db.rawQuery(sql, arrayOf(userId.toString(), date))
        return cursor.use { it.toList { c -> mapCursorWithTags(c) } }
    }

    fun getByCategory(userId: Int, categoryId: Int): List<Task> {
        val db = dbHelper.readableDatabase
        val sql = """
            SELECT t.*, c.${DatabaseHelper.COL_CATEGORY_NAME} as cat_name,
                   c.${DatabaseHelper.COL_CATEGORY_ICON} as cat_icon,
                   c.${DatabaseHelper.COL_CATEGORY_COLOR} as cat_color
            FROM ${DatabaseHelper.TABLE_TASK} t
            LEFT JOIN ${DatabaseHelper.TABLE_CATEGORY} c ON t.${DatabaseHelper.COL_TASK_CATEGORY_ID} = c.${DatabaseHelper.COL_CATEGORY_ID}
            WHERE t.${DatabaseHelper.COL_TASK_USER_ID} = ? AND t.${DatabaseHelper.COL_TASK_CATEGORY_ID} = ?
            ORDER BY t.${DatabaseHelper.COL_TASK_DUE_DATE} ASC
        """.trimIndent()
        val cursor = db.rawQuery(sql, arrayOf(userId.toString(), categoryId.toString()))
        return cursor.use { it.toList { c -> mapCursorWithTags(c) } }
    }

    fun search(userId: Int, keyword: String, tagIds: List<Int> = emptyList()): List<Task> {
        val db = dbHelper.readableDatabase
        val likeKeyword = "%$keyword%"
        val sql = if (tagIds.isEmpty()) {
            """
            SELECT DISTINCT t.*, c.${DatabaseHelper.COL_CATEGORY_NAME} as cat_name,
                   c.${DatabaseHelper.COL_CATEGORY_ICON} as cat_icon,
                   c.${DatabaseHelper.COL_CATEGORY_COLOR} as cat_color
            FROM ${DatabaseHelper.TABLE_TASK} t
            LEFT JOIN ${DatabaseHelper.TABLE_CATEGORY} c ON t.${DatabaseHelper.COL_TASK_CATEGORY_ID} = c.${DatabaseHelper.COL_CATEGORY_ID}
            WHERE t.${DatabaseHelper.COL_TASK_USER_ID} = ?
              AND t.${DatabaseHelper.COL_TASK_TITLE} LIKE ?
            ORDER BY t.${DatabaseHelper.COL_TASK_DUE_DATE} ASC
            """.trimIndent()
        } else {
            val placeholders = tagIds.joinToString(",") { "?" }
            """
            SELECT DISTINCT t.*, c.${DatabaseHelper.COL_CATEGORY_NAME} as cat_name,
                   c.${DatabaseHelper.COL_CATEGORY_ICON} as cat_icon,
                   c.${DatabaseHelper.COL_CATEGORY_COLOR} as cat_color
            FROM ${DatabaseHelper.TABLE_TASK} t
            LEFT JOIN ${DatabaseHelper.TABLE_CATEGORY} c ON t.${DatabaseHelper.COL_TASK_CATEGORY_ID} = c.${DatabaseHelper.COL_CATEGORY_ID}
            LEFT JOIN ${DatabaseHelper.TABLE_TAGS_TASKS} tt ON t.${DatabaseHelper.COL_TASK_ID} = tt.${DatabaseHelper.COL_TT_TASK_ID}
            WHERE t.${DatabaseHelper.COL_TASK_USER_ID} = ?
              AND t.${DatabaseHelper.COL_TASK_TITLE} LIKE ?
              AND tt.${DatabaseHelper.COL_TT_TAG_ID} IN ($placeholders)
            ORDER BY t.${DatabaseHelper.COL_TASK_DUE_DATE} ASC
            """.trimIndent()
        }
        val args = mutableListOf(userId.toString(), likeKeyword)
        args.addAll(tagIds.map { it.toString() })
        val cursor = db.rawQuery(sql, args.toTypedArray())
        return cursor.use { it.toList { c -> mapCursorWithTags(c) } }
    }

    fun getStatsByUser(userId: Int, fromDate: String? = null, toDate: String? = null): StatsSummary {
        val db = dbHelper.readableDatabase
        val dateFilter = if (fromDate != null && toDate != null)
            "AND DATE(${DatabaseHelper.COL_TASK_DUE_DATE}) BETWEEN '$fromDate' AND '$toDate'" else ""
        val cursor = db.rawQuery("""
            SELECT COUNT(*) as total,
                   SUM(CASE WHEN ${DatabaseHelper.COL_TASK_IS_COMPLETED} = 1 THEN 1 ELSE 0 END) as completed
            FROM ${DatabaseHelper.TABLE_TASK}
            WHERE ${DatabaseHelper.COL_TASK_USER_ID} = ? $dateFilter
        """.trimIndent(), arrayOf(userId.toString()))
        return cursor.use {
            if (it.moveToFirst()) {
                val total = it.getInt(0)
                val completed = it.getInt(1)
                val pending = total - completed
                val rate = if (total > 0) completed.toFloat() / total * 100f else 0f
                StatsSummary(total, completed, pending, rate)
            } else StatsSummary(0, 0, 0, 0f)
        }
    }

    fun getTaskById(taskId: Int, userId: Int): Task? {
        val db = dbHelper.readableDatabase
        val sql = """
            SELECT t.*, c.${DatabaseHelper.COL_CATEGORY_NAME} as cat_name,
                   c.${DatabaseHelper.COL_CATEGORY_ICON} as cat_icon,
                   c.${DatabaseHelper.COL_CATEGORY_COLOR} as cat_color
            FROM ${DatabaseHelper.TABLE_TASK} t
            LEFT JOIN ${DatabaseHelper.TABLE_CATEGORY} c ON t.${DatabaseHelper.COL_TASK_CATEGORY_ID} = c.${DatabaseHelper.COL_CATEGORY_ID}
            WHERE t.${DatabaseHelper.COL_TASK_ID} = ? AND t.${DatabaseHelper.COL_TASK_USER_ID} = ?
        """.trimIndent()
        val cursor = db.rawQuery(sql, arrayOf(taskId.toString(), userId.toString()))
        return cursor.use { if (it.moveToFirst()) mapCursorWithTags(it) else null }
    }

    private fun mapCursorWithTags(cursor: Cursor): Task {
        val taskId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TASK_ID))
        val tags = tagRepo.getTagsForTask(taskId)
        return Task(
            taskId = taskId,
            title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TASK_TITLE)),
            dueDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TASK_DUE_DATE)),
            note = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TASK_NOTE)),
            reminderTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TASK_REMINDER_TIME)),
            isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TASK_IS_COMPLETED)) == 1,
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TASK_USER_ID)),
            categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TASK_CATEGORY_ID)),
            categoryName = cursor.getString(cursor.getColumnIndexOrThrow("cat_name")) ?: "",
            categoryIcon = cursor.getString(cursor.getColumnIndexOrThrow("cat_icon")) ?: "",
            categoryColor = cursor.getString(cursor.getColumnIndexOrThrow("cat_color")) ?: "",
            tags = tags
        )
    }

    private fun <T> Cursor.toList(mapper: (Cursor) -> T): List<T> {
        val list = mutableListOf<T>()
        while (moveToNext()) list.add(mapper(this))
        return list
    }
}
