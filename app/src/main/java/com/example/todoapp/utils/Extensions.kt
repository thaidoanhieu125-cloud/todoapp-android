package com.example.todoapp.utils

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.ColorUtils
import com.example.todoapp.data.model.Tag
import java.text.SimpleDateFormat
import java.util.*

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.chooseTags(
    tags: List<Tag>,
    selected: List<Tag>,
    title: String,
    confirmLabel: String,
    onConfirm: (List<Tag>) -> Unit
) {
    if (tags.isEmpty()) {
        toast("Chưa có nhãn nào!")
        return
    }
    val names = tags.map { it.name }.toTypedArray()
    val checked = BooleanArray(tags.size) { index ->
        selected.any { it.tagId == tags[index].tagId }
    }
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMultiChoiceItems(names, checked) { _, index, isChecked -> checked[index] = isChecked }
        .setPositiveButton(confirmLabel) { _, _ ->
            onConfirm(tags.filterIndexed { index, _ -> checked[index] })
        }
        .setNegativeButton("Hủy", null)
        .show()
}

fun View.visible() { visibility = View.VISIBLE }
fun View.gone() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun String.toDisplayDate(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(this)
        date?.let { outputFormat.format(it) } ?: this
    } catch (e: Exception) {
        this
    }
}

fun String.toDisplayDateTime(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(this)
        date?.let { outputFormat.format(it) } ?: this
    } catch (e: Exception) {
        this
    }
}

fun Date.toDbFormat(): String {
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return format.format(this)
}

fun String.toDate(): Date? {
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return try { format.parse(this) } catch (e: Exception) { null }
}

fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.hexToColor(): Int {
    return try { Color.parseColor(this) } catch (e: Exception) { Color.BLUE }
}

fun Int.toContrastTextColor(): Int {
    return if (ColorUtils.calculateLuminance(this) > 0.5) Color.BLACK else Color.WHITE
}
