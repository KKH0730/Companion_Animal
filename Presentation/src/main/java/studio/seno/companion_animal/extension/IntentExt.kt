package studio.seno.companion_animal.extension

import android.content.Context
import android.content.Intent

fun <T> Context.startActivity(
    activityClass: Class<T>,
    builder: (Intent.() -> Unit),
) {
    startActivity(Intent(this, activityClass).apply(builder))
}

fun <T> Context.startActivity(
    activityClass: Class<T>,
) {
    startActivity(Intent(this, activityClass))
}
