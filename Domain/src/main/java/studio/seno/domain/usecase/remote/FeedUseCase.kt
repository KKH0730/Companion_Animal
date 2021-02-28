package studio.seno.domain.usecase.remote

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.StorageReference
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.database.AppDatabase
import studio.seno.domain.model.Feed
import studio.seno.domain.model.User
import studio.seno.domain.usecase.local.LocalUserUseCase
import studio.seno.domain.util.PreferenceManager

class FeedUseCase {
    private val userMangerUseCase = RemoteUserUseCase()
    private val uploadUseCase = UploadUseCase()


    //피드 사진들, 게시자 프로필 사진, 게시자 팔로워를 업로드 후 Feed 객체에 저장한 후 db에 저장
    fun uploadFeed(
        feed: Feed, storageRef: StorageReference,
        mDB: FirebaseFirestore, callback: LongTaskCallback<Feed>
    ) {

        //var remoteProfilePath = feed.email + "/profile/profileImage"
        var remoteImagePath = feed.email + "/feed/" + feed.timestamp + "/"


        uploadUseCase.deleteRemoteFeedImage(feed.email!!, feed.timestamp, storageRef, object : LongTaskCallback<Boolean>{
            override fun onResponse(result: Result<Boolean>) {
                if(result is Result.Success) {

                    uploadUseCase.loadRemoteProfileImage(feed.email!!, storageRef, object : LongTaskCallback<String> {
                        override fun onResponse(result: Result<String>) {
                            if(result is Result.Success) {
                                feed.remoteProfileUri = result.data

                                uploadUseCase.uploadRemoteFeedImage(feed.localUri, remoteImagePath, storageRef, object : LongTaskCallback<Boolean> {
                                    override fun onResponse(result: Result<Boolean>) {
                                        if(result is Result.Success) {

                                            uploadUseCase.loadRemoteFeedImage(remoteImagePath, feed.localUri.size, storageRef, object : LongTaskCallback<List<String>>{
                                                override fun onResponse(result: Result<List<String>>) {
                                                    if(result is Result.Success) {
                                                        feed.remoteUri = result.data

                                                        //db에 객체 데이터 저장
                                                        mDB.collection("feed")
                                                            .document(feed.email + feed.timestamp)
                                                            .set(feed)
                                                            .addOnCompleteListener {
                                                                callback.onResponse(Result.Success(feed))

                                                            }.addOnFailureListener {
                                                                callback.onResponse(Result.Error(it))
                                                            }

                                                        //db에 내가 올린 글 업로드
                                                        mDB.collection("user")
                                                            .document(feed.email!!)
                                                            .collection("myFeed")
                                                            .document(feed.email + feed.timestamp)
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
        userMangerUseCase.updateRemoteUserInfo(feed.email!!, mDB)
    }

    /*
    //피드 사진들, 게시자 프로필 사진, 게시자 팔로워를 업로드 후 Feed 객체에 저장한 후 db에 저장
    fun uploadFeed(
        context: Context, feed: Feed, mDB: FirebaseFirestore,
        storageRef: StorageReference, lifecycleCoroutineScope: LifecycleCoroutineScope,
        callback: LongTaskCallback<Feed>
    ) {

        //var remoteProfilePath = feed.email + "/profile/profileImage"
        var remoteImagePath = feed.email + "/feed/" + feed.timestamp + "/"

        uploadUseCase.deleteRemoteFeedImage(feed, storageRef, object : LongTaskCallback<Boolean>{
            override fun onResponse(result: Result<Boolean>) {
                if(result is Result.Success) {
                    //Feed 이미지 업로드
                    uploadUseCase.uploadRemoteFeedImage(
                        feed,
                        storageRef,
                        remoteImagePath,
                        object : LongTaskCallback<Boolean> {
                            override fun onResponse(result: Result<Boolean>) {

                                LocalUserUseCase().getUserInfo(
                                    lifecycleCoroutineScope,
                                    AppDatabase.getInstance(context)!!,
                                    object : LongTaskCallback<User> {
                                        override fun onResponse(result: Result<User>) {
                                            if (result is Result.Success) {

                                                feed.remoteProfileUri = result.data.profileUri

                                                storageRef.child(remoteImagePath).listAll()
                                                    .addOnCompleteListener { it2 ->
                                                        var listResult = it2.result?.items!!

                                                        //Feed 이미지 로드 후 객체에 저장
                                                        uploadUseCase.loadRemoteFeedImage(
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
                                                                            callback.onResponse(
                                                                                Result.Success(
                                                                                    feed
                                                                                )
                                                                            )

                                                                        }.addOnFailureListener {
                                                                            callback.onResponse(Result.Error(it))
                                                                        }

                                                                    //db에 내가 올린 글 업로드
                                                                    mDB.collection("user")
                                                                        .document(feed.email)
                                                                        .collection("myFeed")
                                                                        .document(feed.email + feed.timestamp)
                                                                        .set(feed)
                                                                }
                                                            })

                                                    }
                                                    .addOnFailureListener { callback.onResponse(Result.Error(it)) }
                                            } else if (result is Result.Error) {
                                                Log.e(
                                                    "error",
                                                    "FeedUseCase uploadFeed error : ${result.exception}"
                                                )
                                            }
                                        }
                                    })
                            }
                        })
                }
            }
        })
        userMangerUseCase.updateRemoteUserInfo(feed.email, mDB)
    }

     */


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
                            val feed = Feed(
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
                                list[i].data?.get("bookmarkList") as Map<String, String>
                            )
                            feedList.add(feed)
                        }
                        callback.onResponse(Result.Success(feedList.toList()))
                    }
                }
            }.addOnFailureListener {
                callback.onResponse(Result.Error(it))
            }
    }

    fun loadFeed(path: String, db: FirebaseFirestore, callback: LongTaskCallback<Feed>) {
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

        db.collection("user")
            .document(feed.email!!)
            .collection("myFeed")
            .document(feed.email + feed.timestamp)
            .delete()



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
        var heartList = feed.heartList.toMutableMap()

        if (flag)
            heartList[myEmail] = myEmail
        else
            heartList.remove(myEmail)


        map["heartList"] = heartList
        map["heart"] = count

        db.collection("feed")
            .document(feed.email + feed.timestamp)
            .update(map)

        db.collection("user")
            .document(myEmail)
            .collection("myFeed")
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

        db.collection("user")
            .document(myEmail)
            .collection("myFeed")
            .document(feed.email + feed.timestamp)
            .update(map)

        if (flag) {
            map = hashMapOf<String, Any>()
            map["feed"] = feed.email + feed.timestamp
            map["timestamp"] = feed.timestamp
            db.collection("user")
                .document(myEmail)
                .collection("bookmark")
                .document(feed.email + feed.timestamp)
                .set(map)
        }
    }


}