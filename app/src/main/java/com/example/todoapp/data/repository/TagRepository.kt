package com.example.todoapp.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.todoapp.data.db.DatabaseHelper
import com.example.todoapp.data.model.Tag

class TagRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun insert(tag: Tag): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_TAG_NAME, tag.name)
            put(DatabaseHelper.COL_TAG_COLOR, tag.color)
            put(DatabaseHelper.COL_TAG_USER_ID, tag.userId)
        }
        return db.insert(DatabaseHelper.TABLE_TAGS, null, values)
    }

    fun update(tag: Tag): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_TAG_NAME, tag.name)
            put(DatabaseHelper.COL_TAG_COLOR, tag.color)
        }
        return db.update(DatabaseHelper.TABLE_TAGS, values,
            "${DatabaseHelper.COL_TAG_ID} = ? AND ${DatabaseHelper.COL_TAG_USER_ID} = ?",
            arrayOf(tag.tagId.toString(), tag.userId.toString()))
    }

    fun delete(tagId: Int, userId: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(DatabaseHelper.TABLE_TAGS,
            "${DatabaseHelper.COL_TAG_ID} = ? AND ${DatabaseHelper.COL_TAG_USER_ID} = ?",
            arrayOf(tagId.toString(), userId.toString()))
    }

    fun getAllByUser(userId: Int): List<Tag> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_TAGS, null,
            "${DatabaseHelper.COL_TAG_USER_ID} = ?", arrayOf(userId.toString()),
            null, null, "${DatabaseHelper.COL_TAG_NAME} ASC")
        return cursor.use { it.toList { c -> mapCursor(c) } }
    }

    fun getTagsForTask(taskId: Int): List<Tag> {
        val db = dbHelper.readableDatabase
        val sql = """
            SELECT t.* FROM ${DatabaseHelper.TABLE_TAGS} t
            INNER JOIN ${DatabaseHelper.TABLE_TAGS_TASKS} tt ON t.${DatabaseHelper.COL_TAG_ID} = tt.${DatabaseHelper.COL_TT_TAG_ID}
            WHERE tt.${DatabaseHelper.COL_TT_TASK_ID} = ?
        """.trimIndent()
        val cursor = db.rawQuery(sql, arrayOf(taskId.toString()))
        return cursor.use { it.toList { c -> mapCursor(c) } }
    }

    private fun mapCursor(cursor: Cursor): Tag {
        return Tag(
            tagId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TAG_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TAG_NAME)),
            color = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TAG_COLOR)) ?: "#FF9800",
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TAG_USER_ID))
        )
    }

    private fun <T> Cursor.toList(mapper: (Cursor) -> T): List<T> {
        val list = mutableListOf<T>()
        while (moveToNext()) list.add(mapper(this))
        return list
    }
}
