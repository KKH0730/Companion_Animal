package studio.seno.domain.repository

import studio.seno.domain.model.NotificationData
import studio.seno.domain.util.LongTaskCallback

interface NotificationRepository {

    fun setNotificationInfo(
        notificationData : NotificationData
    )

    fun getNotification(callback : LongTaskCallback<List<NotificationData>>)

    fun setCheckDot(notificationData : NotificationData)

    fun deleteNotification(
        notificationData : NotificationData,
        callback: LongTaskCallback<Boolean>
    )
}