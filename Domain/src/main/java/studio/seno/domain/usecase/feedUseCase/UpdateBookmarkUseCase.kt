package studio.seno.domain.usecase.feedUseCase

import androidx.lifecycle.LifecycleCoroutineScope
import studio.seno.domain.repository.FeedRepository
import studio.seno.domain.model.Feed
import javax.inject.Inject

class UpdateBookmarkUseCase @Inject constructor(private val repository: FeedRepository) {
    fun execute(feed: Feed, flag: Boolean) {

        repository.updateBookmark(feed, flag)
    }
}