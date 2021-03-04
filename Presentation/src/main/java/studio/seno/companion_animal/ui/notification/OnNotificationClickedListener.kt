package studio.seno.companion_animal.ui.notification

import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import studio.seno.domain.model.NotificationData

interface OnNotificationClickedListener {
    fun onNotificationClicked(notificationLayout : ConstraintLayout, item : NotificationData)
    fun onDeleteClicked(item : NotificationData)
}
