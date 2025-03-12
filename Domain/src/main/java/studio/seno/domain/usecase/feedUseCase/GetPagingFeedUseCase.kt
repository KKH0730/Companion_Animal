package studio.seno.domain.usecase.feedUseCase

import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import studio.seno.domain.repository.FeedRepository
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.model.Feed
import javax.inject.Inject

class GetPagingFeedUseCase @Inject constructor(private val repository: FeedRepository) {
    fun execute(
        f1 : Boolean?,
        f2 : Boolean?,
        f3: Boolean?,
        keyword: String?,
        sort: String,
        myEmail: String?,
        recyclerView: RecyclerView,
        callback : LongTaskCallback<Any>
    ) {
        repository.getPagingFeed(
            f1,
            f2,
            f3,
            keyword,
            sort,
            myEmail,
            recyclerView,
            callback
        )
    }
}