package studio.seno.domain.usecase.remote

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
    private val limit = 15


    fun pagingFeed(
        keyword: String?,
        sort: String,
        myEmail: String?,
        recyclerView: RecyclerView,
        db: FirebaseFirestore,
        callback: LongTaskCallback<List<Feed>>
    ) {
        try {
            firstSearchQuery(sort, myEmail, db)
                .get()
                .addOnCompleteListener {
                    var find = 0
                    var count = 0
                    if (it.isSuccessful) {
                        if (it.result?.size()!! <= 0) {
                            callback.onResponse(Result.Success(null))
                        } else {
                            val document: List<DocumentSnapshot> = it.result!!.documents
                            val size = document.size
                            val list = mutableListOf<Feed>()

                            for (element in document) {
                                val feed = mapperFeed(element)
                                count++

                                if (keyword != null) {
                                    if (keyword != "" && (feed.content.contains(keyword) || feed.hashTags.contains("#$keyword"))) {
                                        list.add(feed)
                                        find++
                                    }

                                    if (find == limit || find == size || count == size || count >= 50) {
                                        callback.onResponse(Result.Success(list))
                                        break
                                    }
                                } else {
                                    list.add(feed)

                                    if (count == limit || count == size) {
                                        callback.onResponse(Result.Success(list))
                                        break
                                    }
                                }
                            }


                            if(find == 0)
                                callback.onResponse(Result.Success(null))

                            lastVisible = it.result!!.documents[0 + count - 1]

                            recyclerView.setOnScrollListener(
                                setScrollListener(
                                    keyword,
                                    sort,
                                    myEmail,
                                    db,
                                    callback
                                )
                            )
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
        sort: String,
        myEmail: String?,
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

                    } else if (recyclerView.layoutManager is StaggeredGridLayoutManager) {
                        layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
                        firstVisibleItemPosition =
                            layoutManager.findFirstVisibleItemPositions(IntArray(3))[0]
                    }
                    visibleItemCount = layoutManager?.childCount
                    totalItemCount = layoutManager?.itemCount


                    if (firstVisibleItemPosition != null) {
                        if (isScrolling && (firstVisibleItemPosition + visibleItemCount!! == totalItemCount) && !isLastItemReached) {
                            isScrolling = false


                            nextSearchQuery(sort, myEmail, db)
                                .get().addOnCompleteListener {
                                    var count = 0
                                    var find = 0

                                    if (it.isSuccessful) {
                                        if (it.result?.size()!! <= 0) {
                                            //callback.onResponse(Result.Success(null))
                                        } else {

                                            val document: List<DocumentSnapshot> =
                                                it.result!!.documents
                                            val size = document.size
                                            val list = mutableListOf<Feed>()

                                            for (element in document) {
                                                val feed = mapperFeed(element)
                                                count++

                                                if (keyword != null) {
                                                    if (feed.content.contains(keyword) || feed.hashTags.contains("#$keyword")) {
                                                        list.add(feed)
                                                        find++
                                                    }

                                                    if (find == limit || find == size || count == size || count >= 50) {
                                                        callback.onResponse(Result.Success(list))
                                                        break
                                                    }
                                                } else {
                                                    list.add(feed)

                                                    if (count == limit) {
                                                        callback.onResponse(Result.Success(list))
                                                        break
                                                    }
                                                }
                                            }

                                            lastVisible = it.result!!.documents[0 + count - 1]
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


    fun firstSearchQuery(sort : String, myEmail: String?, db: FirebaseFirestore): Query {
        if(sort == "myFeed")
            return db.collection("user")
                .document(myEmail!!)
                .collection("myFeed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
        else
            return db.collection("feed")
                .orderBy("timestamp", Query.Direction.DESCENDING)

    }

    fun nextSearchQuery(sort : String, myEmail: String?, db: FirebaseFirestore): Query {
        if(sort == "myFeed")
            return db.collection("user")
                .document(myEmail!!)
                .collection("myFeed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible as DocumentSnapshot)
        else
            return db.collection("feed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible as DocumentSnapshot)
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