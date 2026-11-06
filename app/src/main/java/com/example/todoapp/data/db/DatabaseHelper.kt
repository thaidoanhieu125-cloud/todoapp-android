package com.example.todoapp.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "todo_app.db"
        const val DATABASE_VERSION = 2

        // Table names
        const val TABLE_USER = "user"
        const val TABLE_CATEGORY = "category"
        const val TABLE_TASK = "task"
        const val TABLE_TAGS = "tags"
        const val TABLE_TAGS_TASKS = "tags_tasks"

        // USER columns
        const val COL_USER_ID = "user_id"
        const val COL_USERNAME = "username"
        const val COL_PASSWORD = "password"
        const val COL_PASSWORD_HASHED = "password_hashed"
        const val COL_EMAIL = "email"
        const val COL_FULL_NAME = "full_name"
        const val COL_PHONE = "phone"

        // CATEGORY columns
        const val COL_CATEGORY_ID = "category_id"
        const val COL_CATEGORY_NAME = "name"
        const val COL_CATEGORY_ICON = "icon"
        const val COL_CATEGORY_COLOR = "color"
        const val COL_CATEGORY_USER_ID = "user_id"

        // TASK columns
        const val COL_TASK_ID = "task_id"
        const val COL_TASK_TITLE = "title"
        const val COL_TASK_DUE_DATE = "due_date"
        const val COL_TASK_NOTE = "note"
        const val COL_TASK_REMINDER_TIME = "reminder_time"
        const val COL_TASK_IS_COMPLETED = "is_completed"
        const val COL_TASK_USER_ID = "user_id"
        const val COL_TASK_CATEGORY_ID = "category_id"

        // TAGS columns
        const val COL_TAG_ID = "tag_id"
        const val COL_TAG_NAME = "name"
        const val COL_TAG_COLOR = "color"
        const val COL_TAG_USER_ID = "user_id"

        // TAGS_TASKS columns
        const val COL_TT_TAG_ID = "tag_id"
        const val COL_TT_TASK_ID = "task_id"
        const val COL_TT_USER_ID = "user_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_USER (
                $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USERNAME VARCHAR(25) NOT NULL UNIQUE,
                $COL_PASSWORD VARCHAR(255) NOT NULL,
                $COL_PASSWORD_HASHED INTEGER NOT NULL DEFAULT 0,
                $COL_EMAIL VARCHAR(100) NOT NULL UNIQUE,
                $COL_FULL_NAME NVARCHAR(100),
                $COL_PHONE VARCHAR(20)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_CATEGORY (
                $COL_CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CATEGORY_NAME NVARCHAR(225) NOT NULL,
                $COL_CATEGORY_ICON VARCHAR(225),
                $COL_CATEGORY_COLOR NVARCHAR(225),
                $COL_CATEGORY_USER_ID INTEGER NOT NULL,
                FOREIGN KEY ($COL_CATEGORY_USER_ID) REFERENCES $TABLE_USER($COL_USER_ID) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_TASK (
                $COL_TASK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TASK_TITLE NVARCHAR(225) NOT NULL,
                $COL_TASK_DUE_DATE DATETIME,
                $COL_TASK_NOTE NVARCHAR(225),
                $COL_TASK_REMINDER_TIME DATETIME,
                $COL_TASK_IS_COMPLETED INTEGER DEFAULT 0,
                $COL_TASK_USER_ID INTEGER NOT NULL,
                $COL_TASK_CATEGORY_ID INTEGER NOT NULL,
                FOREIGN KEY ($COL_TASK_USER_ID) REFERENCES $TABLE_USER($COL_USER_ID) ON DELETE CASCADE,
                FOREIGN KEY ($COL_TASK_CATEGORY_ID) REFERENCES $TABLE_CATEGORY($COL_CATEGORY_ID) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_TAGS (
                $COL_TAG_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TAG_NAME NVARCHAR(225) NOT NULL,
                $COL_TAG_COLOR NVARCHAR(10),
                $COL_TAG_USER_ID INTEGER NOT NULL,
                FOREIGN KEY ($COL_TAG_USER_ID) REFERENCES $TABLE_USER($COL_USER_ID) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_TAGS_TASKS (
                $COL_TT_TAG_ID INTEGER NOT NULL,
                $COL_TT_TASK_ID INTEGER NOT NULL,
                $COL_TT_USER_ID INTEGER NOT NULL,
                PRIMARY KEY ($COL_TT_TAG_ID, $COL_TT_TASK_ID),
                FOREIGN KEY ($COL_TT_TAG_ID) REFERENCES $TABLE_TAGS($COL_TAG_ID) ON DELETE CASCADE,
                FOREIGN KEY ($COL_TT_TASK_ID) REFERENCES $TABLE_TASK($COL_TASK_ID) ON DELETE CASCADE
            )
        """.trimIndent())


    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_USER ADD COLUMN $COL_PASSWORD_HASHED INTEGER NOT NULL DEFAULT 0")
        }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }
}
