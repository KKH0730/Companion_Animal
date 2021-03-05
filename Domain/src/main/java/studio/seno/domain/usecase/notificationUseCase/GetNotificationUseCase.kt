package studio.seno.domain.usecase.notificationUseCase

import studio.seno.domain.repository.NotificationRepository
import studio.seno.domain.model.NotificationData
import studio.seno.domain.util.LongTaskCallback

class GetNotificationUseCase(private val notificationRepository: NotificationRepository){
    fun execute(callback : LongTaskCallback<List<NotificationData>>) {
        notificationRepository.getNotification(callback)
    }
}