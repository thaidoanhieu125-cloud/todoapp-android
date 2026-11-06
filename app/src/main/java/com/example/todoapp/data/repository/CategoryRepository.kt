package com.example.todoapp.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.todoapp.data.db.DatabaseHelper
import com.example.todoapp.data.model.Category

class CategoryRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun insert(category: Category): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_CATEGORY_NAME, category.name)
            put(DatabaseHelper.COL_CATEGORY_ICON, category.icon)
            put(DatabaseHelper.COL_CATEGORY_COLOR, category.color)
            put(DatabaseHelper.COL_CATEGORY_USER_ID, category.userId)
        }
        return db.insert(DatabaseHelper.TABLE_CATEGORY, null, values)
    }

    fun update(category: Category): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_CATEGORY_NAME, category.name)
            put(DatabaseHelper.COL_CATEGORY_ICON, category.icon)
            put(DatabaseHelper.COL_CATEGORY_COLOR, category.color)
        }
        return db.update(DatabaseHelper.TABLE_CATEGORY, values,
            "${DatabaseHelper.COL_CATEGORY_ID} = ? AND ${DatabaseHelper.COL_CATEGORY_USER_ID} = ?",
            arrayOf(category.categoryId.toString(), category.userId.toString()))
    }

    fun delete(categoryId: Int, userId: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(DatabaseHelper.TABLE_CATEGORY,
            "${DatabaseHelper.COL_CATEGORY_ID} = ? AND ${DatabaseHelper.COL_CATEGORY_USER_ID} = ? " +
                "AND NOT EXISTS (SELECT 1 FROM ${DatabaseHelper.TABLE_TASK} t " +
                "WHERE t.${DatabaseHelper.COL_TASK_CATEGORY_ID} = " +
                "${DatabaseHelper.TABLE_CATEGORY}.${DatabaseHelper.COL_CATEGORY_ID})",
            arrayOf(categoryId.toString(), userId.toString()))
    }

    fun getAllByUser(userId: Int): List<Category> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_CATEGORY, null,
            "${DatabaseHelper.COL_CATEGORY_USER_ID} = ?", arrayOf(userId.toString()),
            null, null, "${DatabaseHelper.COL_CATEGORY_NAME} ASC")
        return cursor.use { it.toList { c -> mapCursor(c) } }
    }

    fun getById(categoryId: Int, userId: Int): Category? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_CATEGORY, null,
            "${DatabaseHelper.COL_CATEGORY_ID} = ? AND ${DatabaseHelper.COL_CATEGORY_USER_ID} = ?",
            arrayOf(categoryId.toString(), userId.toString()),
            null, null, null)
        return cursor.use { if (it.moveToFirst()) mapCursor(it) else null }
    }

    private fun mapCursor(cursor: Cursor): Category {
        return Category(
            categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_NAME)),
            icon = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_ICON)) ?: "ic_folder",
            color = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_COLOR)) ?: "#2196F3",
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_USER_ID))
        )
    }

    private fun <T> Cursor.toList(mapper: (Cursor) -> T): List<T> {
        val list = mutableListOf<T>()
        while (moveToNext()) list.add(mapper(this))
        return list
    }
}
