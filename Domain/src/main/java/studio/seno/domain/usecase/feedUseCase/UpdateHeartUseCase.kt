package studio.seno.domain.usecase.feedUseCase

import studio.seno.domain.repository.FeedRepository
import studio.seno.domain.model.Feed

class UpdateHeartUseCase(private val repository: FeedRepository) {
    fun execute(
        feed: Feed,
        count: Long,
        flag: Boolean
    ) {
        repository.updateHeart(feed, count, flag)
    }
}