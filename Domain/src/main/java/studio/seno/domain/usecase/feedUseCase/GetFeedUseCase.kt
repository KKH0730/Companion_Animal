package studio.seno.domain.usecase.feedUseCase

import androidx.lifecycle.LifecycleCoroutineScope
import studio.seno.domain.repository.FeedRepository
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.model.Feed

class GetFeedUseCase(private val repository: FeedRepository) {
    fun execute(
        path: String,
        callback: LongTaskCallback<Any>
    ) {
        repository.getFeed(path, callback)
    }
}