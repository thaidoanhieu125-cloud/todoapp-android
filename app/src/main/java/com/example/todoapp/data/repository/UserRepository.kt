package com.example.todoapp.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.todoapp.data.db.DatabaseHelper
import com.example.todoapp.data.model.User

class UserRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context.applicationContext)

    fun register(user: User, password: String): Long {
        val hash = PasswordHash.create(password)
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_USERNAME, user.username)
            put(DatabaseHelper.COL_PASSWORD, hash)
            put(DatabaseHelper.COL_PASSWORD_HASHED, 1)
            put(DatabaseHelper.COL_EMAIL, user.email)
            put(DatabaseHelper.COL_FULL_NAME, user.fullName)
            put(DatabaseHelper.COL_PHONE, user.phone)
        }
        return db.insert(DatabaseHelper.TABLE_USER, null, values)
    }

    fun login(username: String, password: String): User? {
        val account = findAccount(DatabaseHelper.COL_USERNAME, username) ?: return null
        if (!account.matches(password)) return null
        if (!account.hashed && !replacePassword(account, password)) return null
        return account.user
    }

    fun getUserById(userId: Int): User? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USER, null,
            "${DatabaseHelper.COL_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )
        return cursor.use { if (it.moveToFirst()) mapCursorToUser(it) else null }
    }

    fun updateUser(user: User): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_FULL_NAME, user.fullName)
            put(DatabaseHelper.COL_EMAIL, user.email)
            put(DatabaseHelper.COL_PHONE, user.phone)
        }
        return db.update(DatabaseHelper.TABLE_USER, values,
            "${DatabaseHelper.COL_USER_ID} = ?", arrayOf(user.userId.toString()))
    }

    fun changePassword(userId: Int, oldPassword: String, newPassword: String): Boolean {
        val account = findAccount(DatabaseHelper.COL_USER_ID, userId.toString()) ?: return false
        return account.matches(oldPassword) && replacePassword(account, newPassword)
    }

    private fun replacePassword(account: Account, password: String): Boolean {
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_PASSWORD, PasswordHash.create(password))
            put(DatabaseHelper.COL_PASSWORD_HASHED, 1)
        }
        return dbHelper.writableDatabase.update(
            DatabaseHelper.TABLE_USER,
            values,
            "${DatabaseHelper.COL_USER_ID} = ? AND ${DatabaseHelper.COL_PASSWORD} = ? " +
                "AND ${DatabaseHelper.COL_PASSWORD_HASHED} = ?",
            arrayOf(account.user.userId.toString(), account.password, if (account.hashed) "1" else "0")
        ) == 1
    }

    private fun findAccount(column: String, value: String): Account? {
        val cursor = dbHelper.readableDatabase.query(
            DatabaseHelper.TABLE_USER, null, "$column = ?", arrayOf(value), null, null, null
        )
        return cursor.use {
            if (!it.moveToFirst()) return@use null
            Account(
                mapCursorToUser(it),
                it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_PASSWORD)),
                it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_PASSWORD_HASHED)) != 0
            )
        }
    }

    private data class Account(val user: User, val password: String, val hashed: Boolean) {
        fun matches(input: String): Boolean =
            if (hashed) PasswordHash.matches(input, password) else input == password
    }

    fun emailExists(email: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_USER,
            arrayOf(DatabaseHelper.COL_USER_ID),
            "${DatabaseHelper.COL_EMAIL} = ?", arrayOf(email),
            null, null, null)
        return cursor.use { it.count > 0 }
    }

    fun usernameExists(username: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_USER,
            arrayOf(DatabaseHelper.COL_USER_ID),
            "${DatabaseHelper.COL_USERNAME} = ?", arrayOf(username),
            null, null, null)
        return cursor.use { it.count > 0 }
    }

    private fun mapCursorToUser(cursor: Cursor): User {
        return User(
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)),
            username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USERNAME)),
            email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL)),
            fullName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME)) ?: "",
            phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE)) ?: ""
        )
    }
}
