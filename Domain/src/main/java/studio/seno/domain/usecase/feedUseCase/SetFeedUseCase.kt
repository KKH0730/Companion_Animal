package studio.seno.domain.usecase.feedUseCase

import androidx.lifecycle.LifecycleCoroutineScope
import studio.seno.domain.repository.FeedRepository
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Mapper
import studio.seno.domain.model.Feed

class SetFeedUseCase (private val repository: FeedRepository) {
    fun execute(
        email: String,
        nickname: String,
        sort: String,
        hashTags: List<String>,
        localUri: List<String>,
        content: String,
        timestamp: Long,
        callback: LongTaskCallback<Any>
    ) {
        val feed = Mapper.getInstance()!!.mapperToFeed(
            0, email, nickname, sort, hashTags, localUri, content, timestamp
        )
        repository.setFeed(feed, callback)
    }
}
