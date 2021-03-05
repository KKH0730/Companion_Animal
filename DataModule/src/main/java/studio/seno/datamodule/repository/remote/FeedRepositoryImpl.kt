package studio.seno.datamodule.repository.remote

import android.util.Log
import android.widget.AbsListView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import studio.seno.domain.Repository.FeedRepository
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result
import studio.seno.domain.model.Feed

class FeedRepositoryImpl() : FeedRepository {
    private val uploadRepositoryImpl = UploadRepositoryImpl()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storageRef: StorageReference = FirebaseStorage.getInstance()
        .getReferenceFromUrl("gs://companion-animal-f0bfa.appspot.com/")
    private var lastVisible: DocumentSnapshot? = null
    private var isScrolling = false
    private var isLastItemReached = false
    private val limit = 18


    override fun setFeed(feed: Feed, callback: LongTaskCallback<Feed>) {
        var remoteImagePath = feed.getEmail() + "/feed/" + feed.getTimestamp() + "/"

        uploadRepositoryImpl.deleteRemoteFeedImage(feed.getEmail()!!, feed.getTimestamp(), storageRef, object :
            LongTaskCallback<Boolean> {
            override fun onResponse(result: Result<Boolean>) {
                if(result is Result.Success) {

                    uploadRepositoryImpl.getRemoteProfileImage(feed.getEmail()!!, object :
                        LongTaskCallback<String> {
                        override fun onResponse(result: Result<String>) {
                            if(result is Result.Success) {
                                feed.setRemoteProfileUri(result.data)

                                uploadRepositoryImpl.setRemoteFeedImage(feed.getLocalUri(), remoteImagePath, storageRef, object :
                                    LongTaskCallback<Boolean> {
                                    override fun onResponse(result: Result<Boolean>) {
                                        if(result is Result.Success) {

                                            uploadRepositoryImpl.getRemoteFeedImage(remoteImagePath, storageRef, object :
                                                LongTaskCallback<List<String>> {
                                                override fun onResponse(result: Result<List<String>>) {
                                                    if(result is Result.Success) {
                                                        feed.setRemoteUri(result.data)

                                                        //db에 객체 데이터 저장
                                                        db.collection("feed")
                                                            .document(feed.getEmail() + feed.getTimestamp())
                                                            .set(feed)
                                                            .addOnCompleteListener {
                                                                callback.onResponse(
                                                                    Result.Success(feed))

                                                            }.addOnFailureListener {
                                                                callback.onResponse(
                                                                    Result.Error(it))
                                                            }

                                                        //db에 내가 올린 글 업로드
                                                        db.collection("user")
                                                            .document(feed.getEmail()!!)
                                                            .collection("myFeed")
                                                            .document(feed.getEmail() + feed.getTimestamp())
                                                            .set(feed)
                                                    }
                                                }

                                            })
                                        }
                                    }

                                })
                            }
                        }

                    })
                }
            }
        })
        updateRemoteUserInfo(feed.getEmail()!!, db, true)

    }

    override fun getFeed(path: String, callback: LongTaskCallback<Feed>) {
        db.collection("feed")
            .document(path)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    if (it.result != null) {
                        if (it.result!!.exists()) {
                            var feed = Feed(
                                it.result!!.getString("email")!!,
                                it.result!!.getString("nickname")!!,
                                it.result!!.getString("sort")!!,
                                it.result!!.data?.get("hashTags") as MutableList<String>,
                                it.result!!.data?.get("localUri") as MutableList<String>,
                                it.result!!.getString("content")!!,
                                it.result!!.getLong("heart")!!,
                                it.result!!.getLong("comment")!!,
                                it.result!!.getLong("timestamp")!!,
                                it.result!!.getString("remoteProfileUri")!!,
                                it.result!!.data?.get("remoteUri") as MutableList<String>,
                                it.result!!.data?.get("heartList") as Map<String, String>,
                                it.result!!.data?.get("bookmarkList") as Map<String, String>
                            )
                            callback.onResponse(Result.Success(feed))
                        } else {
                            callback.onResponse(Result.Success(null))
                        }
                    }
                }
            }.addOnFailureListener {
                callback.onResponse(Result.Error(it))
            }
    }

    override fun deleteFeed(feed: Feed, callback: LongTaskCallback<Boolean>) {
        var remoteImagePath = feed.getEmail() + "/feed/" + feed.getTimestamp() + "/"

        db.collection("feed")
            .document(feed.getEmail() + feed.getTimestamp())
            .delete()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback.onResponse(Result.Success(true))
                } else {
                    callback.onResponse(Result.Success(false))
                }
            }.addOnFailureListener {
                callback.onResponse(Result.Error(it))
            }

        db.collection("user")
            .document(feed.getEmail()!!)
            .collection("myFeed")
            .document(feed.getEmail() + feed.getTimestamp())
            .delete()

        updateRemoteUserInfo(feed.getEmail()!!, db, false)



        storageRef.child(remoteImagePath).listAll().addOnCompleteListener {
            if (it.result != null) {
                for (element in it.result!!.items) {
                    element.delete()
                }
            }
        }
    }


    override fun updateHeart(
        feed: Feed,
        count: Long,
        flag: Boolean
    ) {
        var map = hashMapOf<String, Any>()
        var heartList = feed.getHeartList().toMutableMap()
        var  myEmail = auth.currentUser.email.toString()

        if (flag)
            heartList[myEmail] = myEmail
        else
            heartList.remove(myEmail)


        map["heartList"] = heartList
        map["heart"] = count

        db.collection("feed")
            .document(feed.getEmail() + feed.getTimestamp())
            .update(map)

        db.collection("user")
            .document(myEmail)
            .collection("myFeed")
            .document(feed.getEmail() + feed.getTimestamp())
            .update(map)
    }

    override fun updateBookmark(feed: Feed, flag: Boolean) {
        var map = hashMapOf<String, Any>()
        val bookmarkList = feed.getBookmarkList().toMutableMap()
        val myEmail = auth.currentUser.email.toString()

        if (flag) {
            bookmarkList[myEmail] = myEmail
        } else {
            bookmarkList.remove(myEmail)

            db.collection("user")
                .document(myEmail)
                .collection("bookmark")
                .document(feed.getEmail() + feed.getTimestamp())
                .delete()
                .addOnFailureListener {

                }

        }

        map["bookmarkList"] = bookmarkList
        db.collection("feed")
            .document(feed.getEmail() + feed.getTimestamp())
            .update(map)

        db.collection("user")
            .document(myEmail)
            .collection("myFeed")
            .document(feed.getEmail() + feed.getTimestamp())
            .update(map)

        if (flag) {
            map = hashMapOf<String, Any>()
            map["feed"] = feed.getEmail() + feed.getTimestamp()
            map["timestamp"] = feed.getTimestamp()
            db.collection("user")
                .document(myEmail)
                .collection("bookmark")
                .document(feed.getEmail() + feed.getTimestamp())
                .set(map)
        }
    }

    private fun updateRemoteUserInfo(email : String, db : FirebaseFirestore, flag : Boolean) {
        db.collection("user")
            .document(email)
            .get()
            .addOnSuccessListener {
                var count : Long? = it.getLong("feedCount")

                if (count != null) {

                    if(flag){
                        db.collection("user")
                            .document(email)
                            .update("feedCount", (count + 1))
                    } else {
                        db.collection("user")
                            .document(email)
                            .update("feedCount", (count - 1))
                    }

                }
            }
    }

    override fun getPagingFeed(
        f1: Boolean?,
        f2: Boolean?,
        f3: Boolean?,
        keyword: String?,
        sort: String,
        myEmail: String?,
        recyclerView: RecyclerView,
        callback : LongTaskCallback<List<Feed>>
    ) {
        try {
            lastVisible = null
            isScrolling = false
            isLastItemReached = false

            firstSearchQuery(sort, myEmail, db)
                .get()
                .addOnCompleteListener {it ->
                    var find = 0
                    var count = 0

                    if (it.isSuccessful) {
                        if (it.result?.size()!! <= 0) {
                            callback.onResponse(Result.Success(null))

                        } else {
                            val document: List<DocumentSnapshot> = it.result!!.documents
                            val size = document.size
                            val list = mutableListOf<Feed>()

                            loop@ for (element in document) {
                                if (sort == "feed_search" && keyword != null) {
                                    count++
                                    val feed = mapperFeed(element)
                                    if (keyword != "" && (feed.getContent().contains(keyword) || feed.getHashTags().contains("#$keyword"))) {
                                        list.add(feed)
                                        find++
                                    }

                                    if (find == limit || find == size) {
                                        callback.onResponse(Result.Success(list))
                                        lastVisible = it.result!!.documents[find - 1]
                                        break@loop
                                    } else if(count == size || count >= 500) {
                                        callback.onResponse(Result.Success(list))
                                        lastVisible = it.result!!.documents[count - 1]
                                        break@loop
                                    }


                                } else if (sort == "feed_timeline") {
                                    count++
                                    val feed = mapperFeed(element)
                                    list.add(feed)

                                    if (count == limit || count == size) {
                                        callback.onResponse(Result.Success(list))
                                        lastVisible = it.result!!.documents[count - 1]
                                        break@loop
                                    }

                                } else if(sort == "feed_list"){
                                    count++
                                    val feed = mapperFeed(element)
                                    if(f1 == true && feed.getSort() == "dog") {
                                        list.add(feed)
                                        find++
                                    } else if(f2 == true &&feed.getSort() == "cat") {
                                        list.add(feed)
                                        find++
                                    } else if(f3 == true && feed.getSort() != "dog" && feed.getSort() != "cat") {
                                        list.add(feed)
                                        find++
                                    }

                                    if (find == limit || find == size) {
                                        callback.onResponse(Result.Success(list))
                                        lastVisible = it.result!!.documents[find - 1]
                                        break@loop
                                    } else if (count == size || count >= 500) {
                                        callback.onResponse(Result.Success(list))
                                        lastVisible = it.result!!.documents[count - 1]
                                        break@loop
                                    }
                                } else if (sort == "feed_bookmark") {
                                    db.collection("feed")
                                        .document(element.getString("feed")!!)
                                        .get()
                                        .addOnCompleteListener {it2 ->
                                            count++

                                            if(it2.result?.exists() == true){
                                                find++
                                                val feedBookmark = mapperFeed(it2.result!!)
                                                list.add(feedBookmark)

                                                if (find == limit || find == size) {
                                                    callback.onResponse(Result.Success(list))
                                                    lastVisible = it.result!!.documents[find - 1]
                                                }
                                            }

                                            if(count == size || count >= 500) {
                                                callback.onResponse(Result.Success(list))
                                                lastVisible = it.result!!.documents[count - 1]
                                                return@addOnCompleteListener
                                            }
                                        }

                                    if(count == size || count >= 500) {
                                        callback.onResponse(Result.Success(list))
                                        lastVisible = it.result!!.documents[count - 1]
                                        break@loop
                                    }
                                }
                            }

                            if (sort == "feed_search" && find == 0) {
                                callback.onResponse(Result.Success(null))
                                return@addOnCompleteListener
                            }

                            recyclerView.setOnScrollListener(
                                setScrollListener(
                                    f1, f2 , f3,
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
        f1 : Boolean?, f2 : Boolean?, f3: Boolean?,
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

                    } else if (recyclerView.layoutManager is GridLayoutManager) {
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
                                        val document: List<DocumentSnapshot> =
                                            it.result!!.documents
                                        val size = document.size
                                        val list = mutableListOf<Feed>()

                                        loop@ for (element in document) {
                                            if (sort == "feed_search" && keyword != null) {
                                                count++
                                                val feed = mapperFeed(element)
                                                if (keyword != "" && (feed.getContent().contains(keyword) || feed.getContent().contains("#$keyword"))) {
                                                    list.add(feed)
                                                    find++
                                                }

                                                if (find == limit || find == size) {
                                                    callback.onResponse(Result.Success(list))
                                                    lastVisible = it.result!!.documents[find - 1]
                                                    break@loop
                                                } else if(count == size || count >= 500) {
                                                    callback.onResponse(Result.Success(list))
                                                    lastVisible = it.result!!.documents[count - 1]
                                                    break@loop
                                                }


                                            } else if (sort == "feed_timeline") {
                                                count++
                                                val feed = mapperFeed(element)
                                                list.add(feed)

                                                if (count == limit || count == size) {
                                                    callback.onResponse(Result.Success(list))
                                                    lastVisible = it.result!!.documents[count - 1]
                                                    break@loop
                                                }

                                            } else if(sort == "feed_list"){
                                                count++
                                                val feed = mapperFeed(element)
                                                if(f1 == true && feed.getSort() == "dog") {
                                                    list.add(feed)
                                                    find++
                                                } else if(f2 == true &&feed.getSort() == "cat") {
                                                    list.add(feed)
                                                    find++
                                                } else if(f3 == true && feed.getSort() != "dog" && feed.getSort() != "cat") {
                                                    list.add(feed)
                                                    find++
                                                }

                                                if (find == limit || find == size) {
                                                    callback.onResponse(Result.Success(list))
                                                    lastVisible = it.result!!.documents[find - 1]
                                                    break@loop
                                                } else if (count == size || count >= 500) {
                                                    callback.onResponse(Result.Success(list))
                                                    lastVisible = it.result!!.documents[count - 1]
                                                    break@loop
                                                }
                                            } else if (sort == "feed_bookmark") {
                                                db.collection("feed")
                                                    .document(element.getString("feed")!!)
                                                    .get()
                                                    .addOnCompleteListener {it2 ->
                                                        count++

                                                        if(it2.result?.exists() == true){
                                                            find++
                                                            val feedBookmark = mapperFeed(it2.result!!)
                                                            list.add(feedBookmark)

                                                            if (find == limit || find == size) {
                                                                callback.onResponse(
                                                                    Result.Success(list))
                                                                lastVisible = it.result!!.documents[find - 1]
                                                            }
                                                        }

                                                        if(count == size || count >= 500) {
                                                            callback.onResponse(Result.Success(list))
                                                            lastVisible = it.result!!.documents[count - 1]
                                                            return@addOnCompleteListener
                                                        }
                                                    }

                                                if(count == size || count >= 500) {
                                                    callback.onResponse(Result.Success(list))
                                                    lastVisible = it.result!!.documents[count - 1]
                                                    break@loop
                                                }
                                            }
                                        }

                                        if (document.size < limit) {
                                            isLastItemReached = true
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



    fun firstSearchQuery(sort: String, myEmail: String?, db: FirebaseFirestore): Query {
        if (sort == "feed_timeline")
            return db.collection("user")
                .document(myEmail!!)
                .collection("myFeed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
        else if (sort == "feed_search" || sort == "feed_list")
            return db.collection("feed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
        else
            return db.collection("user")
                .document(myEmail!!)
                .collection("bookmark")
                .orderBy("timestamp", Query.Direction.DESCENDING)

    }

    fun nextSearchQuery(sort: String, myEmail: String?, db: FirebaseFirestore): Query {
        if (sort == "feed_timeline")
            return db.collection("user")
                .document(myEmail!!)
                .collection("myFeed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible as DocumentSnapshot)
        else if (sort == "feed_search" || sort == "feed_list")
            return db.collection("feed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible as DocumentSnapshot)
        else
            return db.collection("user")
                .document(myEmail!!)
                .collection("bookmark")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible as DocumentSnapshot)
    }

    private fun mapperFeed(document: DocumentSnapshot): Feed {
        return Feed(
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
    }
}