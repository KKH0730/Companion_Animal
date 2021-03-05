package studio.seno.domain.Repository

import androidx.recyclerview.widget.RecyclerView
import studio.seno.domain.model.Feed
import studio.seno.domain.util.LongTaskCallback

interface FeedRepository {
    fun setFeed(
        feed: Feed,
        callback: LongTaskCallback<Feed>
    )

    fun getPagingFeed(
        f1 : Boolean?,
        f2 : Boolean?,
        f3: Boolean?,
        keyword: String?,
        sort: String,
        myEmail: String?,
        recyclerView: RecyclerView,
        callback : LongTaskCallback<List<Feed>>
    )

    fun getFeed(
        path: String,
        callback: LongTaskCallback<Feed>
    )

    fun deleteFeed(
        feed: Feed,
        callback: LongTaskCallback<Boolean>
    )


    fun updateHeart(
        feed: Feed,
        count: Long,
        flag: Boolean
    )

    fun updateBookmark(
        feed: Feed,
        flag: Boolean
    )
}