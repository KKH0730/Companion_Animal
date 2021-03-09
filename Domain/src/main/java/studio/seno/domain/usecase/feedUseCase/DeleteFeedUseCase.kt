package studio.seno.domain.usecase.feedUseCase

import androidx.lifecycle.LifecycleCoroutineScope
import studio.seno.domain.repository.FeedRepository
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.model.Feed

class DeleteFeedUseCase(private val repository: FeedRepository){
    fun execute(
        feed: Feed,
        callback: LongTaskCallback<Any>
    ) {

        repository.deleteFeed(feed, callback)
    }
}