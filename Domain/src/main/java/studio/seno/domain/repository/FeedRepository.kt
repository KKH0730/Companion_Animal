package studio.seno.domain.repository

import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import studio.seno.domain.model.Feed
import studio.seno.domain.util.LongTaskCallback

interface FeedRepository {
    fun setFeed(
        feed: Feed,
        callback: LongTaskCallback<Any>
    )

    fun getPagingFeed(
        f1 : Boolean?,
        f2 : Boolean?,
        f3: Boolean?,
        keyword: String?,
        sort: String,
        myEmail: String?,
        recyclerView: RecyclerView,
        callback : LongTaskCallback<Any>
    )

    fun getFeed(
        path: String,
        callback: LongTaskCallback<Any>
    )

    fun deleteFeed(
        feed: Feed,
        callback: LongTaskCallback<Any>
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