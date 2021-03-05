package studio.seno.domain.usecase.feedUseCase

import studio.seno.domain.Repository.FeedRepository
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.model.Feed

class DeleteFeedUseCase(private val repository: FeedRepository){
    fun execute(
        feed: Feed,
        callback: LongTaskCallback<Boolean>
    ) {

        repository.deleteFeed(feed, callback)
    }
}