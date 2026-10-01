package com.voidlinux.core.designsystem

import android.content.Context
import android.view.View
import com.google.android.material.snackbar.Snackbar

object Components {

    fun showSnack(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
    }

    fun showSnackLong(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
    }

    fun dpToPx(context: Context, dp: Int): Int =
        (dp * context.resources.displayMetrics.density).toInt()
}