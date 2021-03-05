package studio.seno.domain.usecase.notificationUseCase

import studio.seno.domain.Repository.NotificationRepository
import studio.seno.domain.model.NotificationData

class SetCheckDotUseCase(private val notificationRepository: NotificationRepository){
    fun execute(notificationData : NotificationData) {
        notificationRepository.setCheckDot(notificationData)
    }
}