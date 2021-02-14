package studio.seno.companion_animal.ui.notification

import android.widget.ImageView
import studio.seno.domain.model.NotificationData

interface OnNotificationClickedListener {
    fun onNotificationClickced(checkImage : ImageView, item : NotificationData)
}