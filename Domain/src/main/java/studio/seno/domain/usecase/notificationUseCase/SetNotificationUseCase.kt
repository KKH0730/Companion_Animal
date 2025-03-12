package studio.seno.domain.usecase.notificationUseCase

import studio.seno.domain.repository.NotificationRepository
import studio.seno.domain.model.NotificationData
import javax.inject.Inject

class SetNotificationUseCase @Inject constructor(private val notificationRepository: NotificationRepository){
    fun execute( notificationData : NotificationData) {
        notificationRepository.setNotificationInfo(notificationData)
    }
}