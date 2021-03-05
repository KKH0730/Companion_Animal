package studio.seno.domain.usecase.notificationUseCase

import studio.seno.domain.repository.NotificationRepository
import studio.seno.domain.model.NotificationData
import studio.seno.domain.util.LongTaskCallback

class DeleteNotificationUseCase(private val notificationRepository: NotificationRepository){
    fun execute(
        notificationData : NotificationData,
        callback: LongTaskCallback<Boolean>
    ) {
        notificationRepository.deleteNotification(notificationData, callback)
    }
}