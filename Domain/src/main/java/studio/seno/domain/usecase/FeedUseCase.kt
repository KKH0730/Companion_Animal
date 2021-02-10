package studio.seno.domain.usecase

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.*
import com.google.firebase.storage.StorageReference
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.database.InfoManager
import studio.seno.domain.model.Feed

class FeedUseCase {
    private val userMangerUseCase = UserManageUseCase()

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

                                                loadFollower(feed, mDB, object: LongTaskCallback<MutableMap<String, String>>{
                                                    override fun onResponse(result : Result<MutableMap<String, String>>) {
                                                        if(result is Result.Success) {
                                                            feed.followList = result.data.toMap()
                                                            var tempMap : MutableMap<String, String> = feed.followList!!.toMutableMap()

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

                                                                    InfoManager.setLong(
                                                                        context,
                                                                        "feedCount",
                                                                        InfoManager.getLong(
                                                                            context,
                                                                            "feedCount"
                                                                        ) + 1L
                                                                    )
                                                                }.addOnFailureListener { callback.onResponse(Result.Error(it)) }

                                                        }
                                                    }
                                                })
                                            }
                                        })

                                }.addOnFailureListener { callback.onResponse(Result.Error(it)) }
                        }.addOnFailureListener { callback.onResponse(Result.Error(it)) }
                }
            })
        var list = mutableListOf("feedCount")
        userMangerUseCase.updateRemoteUserInfo(feed.email!!, mDB, list)

    }

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
                                list[i].data?.get("bookmarkList") as  Map<String, String>,
                                list[i].data?.get("followList") as  Map<String, String>
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


    fun deleteFeed(feed: Feed, db : FirebaseFirestore,
                   storageRef: StorageReference, callback : LongTaskCallback<Boolean>){
        var remoteImagePath = feed.email + "/feed/" + feed.timestamp + "/"

        db.collection("feed")
            .document(feed.email + feed.timestamp)
            .delete()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    callback.onResponse(Result.Success(true))
                } else{
                    callback.onResponse(Result.Success(false))
                }
            }.addOnFailureListener {
                callback.onResponse(Result.Error(it))
            }

        storageRef.child(remoteImagePath).listAll().addOnCompleteListener{
            if(it.result != null) {
                for(element in it.result!!.items) {
                    element.delete()
                }
            }

        }
    }

    fun updateHeart(feed: Feed, count: Long,  myEmail : String, flag : Boolean, db : FirebaseFirestore){
        var map = hashMapOf<String, Any>()
        var heartList = feed.heartList!!.toMutableMap()

        if(flag)
            heartList[myEmail] = myEmail
        else
            heartList.remove(myEmail)


        map["heartList"] = heartList
        map["heart"] = count

        db.collection("feed")
            .document(feed.email + feed.timestamp)
            .update(map)
    }

    fun updateBookmark(feed: Feed, myEmail: String, flag: Boolean, db: FirebaseFirestore) {
        var map = hashMapOf<String, Any>()
        var bookmarkList = feed.bookmarkList!!.toMutableMap()

        if(flag) {
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

        if(flag) {
            map = hashMapOf<String, Any>()
            map["feed"] = feed.email + feed.timestamp
            db.collection("user")
                .document(myEmail)
                .collection("bookmark")
                .document(feed.email + feed.timestamp)
                .set(map)
        }
    }



    fun loadFollower(feed: Feed, db: FirebaseFirestore, callback : LongTaskCallback<MutableMap<String, String>>){
        db.collection("user")
            .document(feed.email!!)
            .collection("follower")
            .get()
            .addOnCompleteListener {
                val map = mutableMapOf<String, String>()
                var size : Int? = null
                if(it.result != null) {
                    size = it.result!!.size()
                    for(i in 0 until it.result!!.size()) {
                        var document : List<DocumentSnapshot> = it.result!!.documents
                        for(element in document) {
                            var follower = element.getString("email")
                            if (follower != null) {
                                map[follower] = follower
                            }
                        }
                    }

                    if(map.size == size)
                        callback.onResponse(Result.Success(map))
                }
            }
    }

    fun updateFollower(targetFeed: Feed, myEmail: String, flag: Boolean, db: FirebaseFirestore) {
        //flag = true 이면
       //User 콜렉션 :targetEmail의 follower 수 (+1)와 myemail의 following (+1) 수 업데이트
       //User 콜렉션 :targetEmail의 follower 컬렉션에 myemail 업로드, myemail의 following 컬렉션에 targetEmail 업로드
       //feed 콜렉션 :feed 게시물에 follower myEmail 업데이트

        //flag = false이면
        //User 콜렉션 :targetEmail의 follower 수(-1)와 myemail의 following(-1) 수 업데이트
        //User 콜렉션 :targetEmail의 follower 컬렉션에 myemail 삭제, myemail의 following 컬렉션에 targetEmail 삭제
        //feed 콜렉션 :feed 게시물에 follower myEmail 삭제
        if(flag) {
            followerNumberUpdate(targetFeed.email!!, "follower", db, true)
            followerNumberUpdate(myEmail, "following", db, true)

            followerStatusUpdate(targetFeed.email, myEmail, "follower", myEmail, db, true)
            followerStatusUpdate(myEmail, targetFeed.email, "following", targetFeed.email, db, true)
        } else {
            followerNumberUpdate(targetFeed.email!!, "follower", db, false)
            followerNumberUpdate(myEmail, "following", db, false)

            followerStatusUpdate(targetFeed.email, myEmail, "follower", myEmail, db, true)
            followerStatusUpdate(myEmail, targetFeed.email, "following", targetFeed.email, db, true)
        }
    }

    fun followerNumberUpdate(email: String, fieldName : String, db: FirebaseFirestore, add : Boolean) {
        db.collection("user")
            .document(email)
            .get()
            .addOnCompleteListener {
                if(it.result != null) {
                    var count = it.result!!.getLong(fieldName)

                    if(add) {
                        db.collection("user").document(email).update(fieldName, count!! + 1L)
                    } else {
                        db.collection("user").document(email).update(fieldName, count!! - 1L)
                    }
                }

            }
    }

    fun followerStatusUpdate(document1 : String, document2: String, fieldName: String, updateEmail : String, db: FirebaseFirestore, add : Boolean) {
        if(add) {
            var map = mutableMapOf<String, String>()
            map["email"] = updateEmail

            db.collection("user")
                .document(document1)
                .collection(fieldName)
                .document(document2)
                .set(map)
        } else {
            db.collection("user")
                .document(document1)
                .collection(fieldName)
                .document(document2)
                .delete()
        }

    }
}