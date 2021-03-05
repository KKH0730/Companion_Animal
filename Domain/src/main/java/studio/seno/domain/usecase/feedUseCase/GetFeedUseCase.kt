package studio.seno.domain.usecase.feedUseCase

import studio.seno.domain.repository.FeedRepository
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.model.Feed

class GetFeedUseCase(private val repository: FeedRepository) {
    fun execute(
        path: String,
        callback: LongTaskCallback<Feed>
    ) {
        repository.getFeed(path, callback)
    }
}