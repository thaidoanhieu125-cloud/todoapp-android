package com.example.todoapp.ui.auth

import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.todoapp.R
import com.example.todoapp.utils.toast

internal fun <T> AppCompatActivity.runAccountAction(
    button: Button,
    action: () -> T,
    onSuccess: (T) -> Unit
) {
    val label = button.text
    button.isEnabled = false
    button.setText(R.string.account_action_in_progress)
    Thread {
        val result = runCatching(action)
        runOnUiThread {
            if (isFinishing || isDestroyed) return@runOnUiThread
            button.text = label
            button.isEnabled = true
            result.onSuccess(onSuccess).onFailure {
                toast("Không thể thực hiện thao tác. Vui lòng thử lại.")
            }
        }
    }.start()
}
