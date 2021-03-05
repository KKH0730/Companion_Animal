package studio.seno.domain.usecase.feedUseCase

import studio.seno.domain.Repository.FeedRepository
import studio.seno.domain.model.Feed

class UpdateBookmarkUseCase(private val repository: FeedRepository) {
    fun execute(feed: Feed, flag: Boolean) {

        repository.updateBookmark(feed, flag)
    }
}