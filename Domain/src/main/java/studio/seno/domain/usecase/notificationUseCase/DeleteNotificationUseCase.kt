package studio.seno.domain.usecase.notificationUseCase

import studio.seno.domain.repository.NotificationRepository
import studio.seno.domain.model.NotificationData
import studio.seno.domain.util.LongTaskCallback
import javax.inject.Inject

class DeleteNotificationUseCase @Inject constructor(private val notificationRepository: NotificationRepository){
    fun execute(
        notificationData : NotificationData,
        callback: LongTaskCallback<Any>
    ) {
        notificationRepository.deleteNotification(notificationData, callback)
    }
}