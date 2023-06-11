package ir.amirroid.amirmusics.utils

import android.content.Context
import android.net.Uri

val Uri?.isEmpty: Boolean
    get() {
        return this == null
    }

val Uri?.isNotEmpty: Boolean
    get() {
        return this != null
    }

val String.hasZero: Boolean
    get() {
        return this == "0"
    }


fun Int.dp(context: Context) = this * context.resources.displayMetrics.density


fun Float.minWidthRadius(context: Context): Boolean {
    return context.resources.displayMetrics.widthPixels.div(2) >= this
}