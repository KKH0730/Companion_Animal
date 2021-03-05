package studio.seno.domain.usecase.feedUseCase

import androidx.recyclerview.widget.RecyclerView
import studio.seno.domain.Repository.FeedRepository
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.model.Feed

class GetPagingFeedUseCase (private val repository: FeedRepository) {
    fun execute(
        f1 : Boolean?,
        f2 : Boolean?,
        f3: Boolean?,
        keyword: String?,
        sort: String,
        myEmail: String?,
        recyclerView: RecyclerView,
        callback : LongTaskCallback<List<Feed>>
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