package studio.seno.domain.usecase

import android.util.Log
import android.widget.AbsListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Feed

class PagingModule {
    private var lastVisible: DocumentSnapshot? = null
    private var isScrolling = false
    private var isLastItemReached = false
    private val limit = 6L


    fun pagingFeed(
        keyword: String?,
        recyclerView: RecyclerView,
        db: FirebaseFirestore,
        callback: LongTaskCallback<List<Feed>>
    ) {

        try {
            firstSearchQuery(keyword, db)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (it.result?.size()!! <= 0) {
                            callback.onResponse(Result.Success(null))
                        } else {
                            val document: List<DocumentSnapshot> = it.result!!.documents
                            val size = document.size
                            val list = mutableListOf<Feed>()
                            for (element in document) {
                                val feed = mapperFeed(element)
                                list.add(feed)

                                if (size == list.size) {
                                    callback.onResponse(Result.Success(list))
                                }
                            }
                            lastVisible = it.result!!.documents[it.result!!.size() - 1]

                            recyclerView.setOnScrollListener(setScrollListener(keyword, db, callback))
                        }
                    }
                }.addOnFailureListener {
                    callback.onResponse(Result.Error(it))
                }
        } catch (e: Exception) {
            Log.e("error", "SearchUseCase error : ${e.message}")
        }
    }



    fun setScrollListener(
        keyword: String?,
        db: FirebaseFirestore,
        callback: LongTaskCallback<List<Feed>>
    ): RecyclerView.OnScrollListener {
        var onScrollListener: RecyclerView.OnScrollListener =
            object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                        isScrolling = true
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    var layoutManager: RecyclerView.LayoutManager? = null
                    var firstVisibleItemPosition: Int? = null
                    var visibleItemCount: Int? = null
                    var totalItemCount: Int? = null

                    if (recyclerView.layoutManager is LinearLayoutManager) {
                        layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    } else if(recyclerView.layoutManager is StaggeredGridLayoutManager){
                        layoutManager  = recyclerView.layoutManager as StaggeredGridLayoutManager
                        firstVisibleItemPosition = layoutManager.findFirstVisibleItemPositions(IntArray(3))[0]
                    }
                    visibleItemCount = layoutManager?.childCount
                    totalItemCount = layoutManager?.itemCount


                    if (firstVisibleItemPosition != null) {
                        if (isScrolling && (firstVisibleItemPosition + visibleItemCount!! == totalItemCount) && !isLastItemReached) {
                            isScrolling = false

                            nextSearchQuery(keyword, db)
                                .get().addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        if (it.result?.size()!! <= 0) {
                                            callback.onResponse(Result.Success(null))
                                        } else {

                                            val document: List<DocumentSnapshot> =
                                                it.result!!.documents
                                            val size = document.size
                                            val list = mutableListOf<Feed>()

                                            for (element in document) {
                                                val feed = mapperFeed(element)
                                                list.add(feed)
                                                if (size == list.size)
                                                    callback.onResponse(Result.Success(list))
                                            }
                                            lastVisible =
                                                it.result!!.documents[it.result!!.size() - 1]

                                            if (document.size < limit) {
                                                isLastItemReached = true
                                            }
                                        }
                                    }
                                }.addOnFailureListener {
                                    callback.onResponse(Result.Error(it))
                                }
                        }
                    }

                }
            }
        return onScrollListener
    }


    fun firstSearchQuery(keyword: String?, db: FirebaseFirestore): Query {
        val collectionRef =
            db.collection("feed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
        return if (keyword == null) {
            collectionRef
        } else {
            collectionRef.whereArrayContains("hashTags", keyword)
        }
    }

    fun nextSearchQuery(keyword: String?, db: FirebaseFirestore): Query {
        val collectionRef =
            db.collection("feed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible as DocumentSnapshot)
                .limit(limit)
        return if (keyword == null) {
            return collectionRef
        } else {
            collectionRef.whereArrayContains("hashTags", keyword)
        }
    }


    private fun mapperFeed(document: DocumentSnapshot): Feed {
        val feed = Feed(
            document.getString("email")!!,
            document.getString("nickname")!!,
            document.getString("sort")!!,
            document.data?.get("hashTags") as MutableList<String>,
            document.data?.get("localUri") as MutableList<String>,
            document.getString("content")!!,
            document.getLong("heart")!!,
            document.getLong("comment")!!,
            document.getLong("timestamp")!!,
            document.getString("remoteProfileUri")!!,
            document.data?.get("remoteUri") as MutableList<String>,
            document.data?.get("heartList") as Map<String, String>,
            document.data?.get("bookmarkList") as Map<String, String>
        )
        return feed
    }



}