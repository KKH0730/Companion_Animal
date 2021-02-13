package studio.seno.domain.usecase

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.StorageReference
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Feed
import studio.seno.domain.util.PrefereceManager

class FeedUseCase {
    private val userMangerUseCase = UserManageUseCase()

    //피드 사진들, 게시자 프로필 사진, 게시자 팔로워를 업로드 후 Feed 객체에 저장한 후 db에 저장
    fun uploadFeed(
        context: Context, feed: Feed, mDB: FirebaseFirestore,
        storageRef: StorageReference, callback: LongTaskCallback<Boolean>
    ) {
        var remoteProfilePath = feed.email + "/profile/profileImage"
        var remoteImagePath = feed.email + "/feed/" + feed.timestamp + "/"

        //Feed 이미지 업로드
        UploadUseCase().uploadRemoteFeedImage(
            feed,
            storageRef,
            remoteImagePath,
            object : LongTaskCallback<Boolean> {
                override fun onResponse(result: Result<Boolean>) {

                    //프로필 이미지 객체 불러와 객체에 저장
                    storageRef.child(remoteProfilePath)
                        .downloadUrl
                        .addOnSuccessListener { it ->
                            feed.remoteProfileUri = it.toString()


                            storageRef.child(remoteImagePath).listAll()
                                .addOnCompleteListener { it2 ->
                                    var listResult = it2.result?.items!!


                                    //Feed 이미지 로드 후 객체에 저장
                                    UploadUseCase().loadRemoteFeedImage(
                                        listResult,
                                        object : LongTaskCallback<MutableList<String>> {
                                            override fun onResponse(result: Result<MutableList<String>>) {
                                                var res = (result as Result.Success).data
                                                feed.remoteUri = res


                                                //db에 객체 데이터 저장
                                                mDB.collection("feed")
                                                    .document(feed.email + feed.timestamp)
                                                    .set(feed)
                                                    .addOnCompleteListener {
                                                        var result: Result<Boolean>? = null
                                                        if (it.isSuccessful)
                                                            result = Result.Success(true)
                                                        else
                                                            result = Result.Success(false)
                                                        callback.onResponse(result)

                                                        PrefereceManager.setLong(
                                                            context,
                                                            "feedCount",
                                                            PrefereceManager.getLong(
                                                                context,
                                                                "feedCount"
                                                            ) + 1L
                                                        )
                                                    }.addOnFailureListener {
                                                        callback.onResponse(Result.Error(it))
                                                    }
                                            }
                                        })

                                }.addOnFailureListener { callback.onResponse(Result.Error(it)) }
                        }.addOnFailureListener { callback.onResponse(Result.Error(it)) }
                }
            })
        var list = mutableListOf("feedCount")
        userMangerUseCase.updateRemoteUserInfo(feed.email!!, mDB, list)

    }

    //피드 리스트를 불러온다.
    fun loadFeedList(
        db: FirebaseFirestore,
        callback: LongTaskCallback<List<Feed>>
    ) {
        db.collection("feed")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    if (it.result != null) {
                        val snap: QuerySnapshot = it.result!!
                        val list = snap.documents
                        val feedList: MutableList<Feed> = mutableListOf()

                        for (i in 0 until list.size) {
                            var feed = Feed(
                                list[i].getString("email")!!,
                                list[i].getString("nickname")!!,
                                list[i].getString("sort")!!,
                                list[i].data?.get("hashTags") as MutableList<String>,
                                list[i].data?.get("localUri") as MutableList<String>,
                                list[i].getString("content")!!,
                                list[i].getLong("heart")!!,
                                list[i].getLong("comment")!!,
                                list[i].getLong("timestamp")!!,
                                list[i].getString("remoteProfileUri")!!,
                                list[i].data?.get("remoteUri") as MutableList<String>,
                                list[i].data?.get("heartList") as Map<String, String>,
                                list[i].data?.get("bookmarkList") as Map<String, String>,
                            )
                            feedList.add(feed)
                        }
                        var result = Result.Success(feedList.toList())
                        callback.onResponse(result)
                    }
                }
            }.addOnFailureListener {
                callback.onResponse(Result.Error(it))
            }
    }

    //피드를 삭제한다.
    fun deleteFeed(
        feed: Feed, db: FirebaseFirestore,
        storageRef: StorageReference, callback: LongTaskCallback<Boolean>
    ) {
        var remoteImagePath = feed.email + "/feed/" + feed.timestamp + "/"

        db.collection("feed")
            .document(feed.email + feed.timestamp)
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

        storageRef.child(remoteImagePath).listAll().addOnCompleteListener {
            if (it.result != null) {
                for (element in it.result!!.items) {
                    element.delete()
                }
            }

        }
    }

    //좋아요의 상태를 업데이트한다.
    fun updateHeart(
        feed: Feed,
        count: Long,
        myEmail: String,
        flag: Boolean,
        db: FirebaseFirestore
    ) {
        var map = hashMapOf<String, Any>()
        var heartList = feed.heartList!!.toMutableMap()

        if (flag)
            heartList[myEmail] = myEmail
        else
            heartList.remove(myEmail)


        map["heartList"] = heartList
        map["heart"] = count

        db.collection("feed")
            .document(feed.email + feed.timestamp)
            .update(map)
    }

    //북마크의 상태를 업데이트한다.
    fun updateBookmark(feed: Feed, myEmail: String, flag: Boolean, db: FirebaseFirestore) {
        var map = hashMapOf<String, Any>()
        var bookmarkList = feed.bookmarkList!!.toMutableMap()

        if (flag) {
            bookmarkList[myEmail] = myEmail
        } else {
            bookmarkList.remove(myEmail)

            db.collection("user")
                .document(myEmail)
                .collection("bookmark")
                .document(feed.email + feed.timestamp)
                .delete()
                .addOnFailureListener {

                }
        }

        map["bookmarkList"] = bookmarkList
        db.collection("feed")
            .document(feed.email + feed.timestamp)
            .update(map)

        if (flag) {
            map = hashMapOf<String, Any>()
            map["feed"] = feed.email + feed.timestamp
            db.collection("user")
                .document(myEmail)
                .collection("bookmark")
                .document(feed.email + feed.timestamp)
                .set(map)
        }
    }

}