package studio.seno.domain.usecase

import android.content.Context
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.database.InfoManager
import studio.seno.domain.model.Comment
import studio.seno.domain.model.Feed
import studio.seno.domain.model.User

class FeedUseCase {
    private val userMangerUseCase = UserManageUseCase()

    fun uploadFeed(context : Context,
                   feed: Feed, auth: FirebaseAuth, mDB: FirebaseFirestore,
                   storageRef: StorageReference, callback: LongTaskCallback<Boolean>
    ) {
        var remoteProfilePath = auth.currentUser?.email + "/profile/profileImage"
        var remoteImagePath = auth.currentUser?.email + "/feed/" + feed.timestamp + "/"

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


                            storageRef.child(remoteImagePath).listAll().addOnCompleteListener { it2 ->
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

                                                    InfoManager.setLong(context, "feedCount", InfoManager.getLong(context, "feedCount") + 1L)
                                                }.addOnFailureListener {
                                                    Log.e("db", "feed_save_error ${it.message}")
                                                }
                                        }
                                    })

                            }
                        }
                }
            })
        var list = mutableListOf("feedCount")
        userMangerUseCase.updateRemoteUserInfo(auth.currentUser?.email.toString(), mDB, list)

    }

    fun loadFeedList(auth: FirebaseAuth, db: FirebaseFirestore, storageRef: StorageReference, callback: LongTaskCallback<List<Feed>>) {
        db.collection("feed")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    if (it.result != null) {
                        val snap: QuerySnapshot = it.result!!
                        val list = snap.documents
                        val feedList : MutableList<Feed> = mutableListOf()

                        for (i in 0 until list.size) {
                            var feed = Feed(
                                list[i].getString("email")!!, list[i].getString("nickname")!!,
                                list[i].getString("sort")!!, list[i].data?.get("hashTags") as MutableList<String>,
                                list[i].data?.get("localUri") as MutableList<String>, list[i].getString("content")!!,
                                list[i].getLong("heart")!!, list[i].getLong("comment")!!, list[i].getLong("timestamp")!!,
                                list[i].getString("remoteProfileUri")!!,  list[i].data?.get("remoteUri") as MutableList<String>
                            )
                            feedList.add(feed)
                        }
                        var result = Result.Success(feedList.toList())
                        callback.onResponse(result)
                    }
                }
            }.addOnFailureListener{
                Log.d("hi", "error : ${it.message}")
            }
    }

    fun uploadComment(feed : Feed, comment : Comment, auth: FirebaseAuth, db: FirebaseFirestore, storageRef : StorageReference) {
        var remoteProfilePath = auth.currentUser?.email + "/profile/profileImage"
        storageRef.child(remoteProfilePath).downloadUrl.addOnSuccessListener {
            comment.profileUri = it.toString()

            db.collection("feed")
                .document(feed.email + feed.timestamp)
                .collection("comment")
                .document(auth.currentUser?.email.toString() + comment.timestamp)
                .set(comment)
        }
    }

    fun uploadCommentCount(feed : Feed, commentCount : Long, db: FirebaseFirestore) {
        db.collection("feed")
            .document(feed.email + feed.timestamp)
            .update("comment", commentCount + 1)
    }

    fun loadComment(email : String, timestamp : Long, db: FirebaseFirestore, callback : LongTaskCallback<Comment>) {
        db.collection("feed")
            .document(email + timestamp)
            .collection("comment")
            .orderBy("timestamp")
            .get()
            .addOnCompleteListener {


                if(it.result != null) {
                    for(i in 0 until it.result!!.size()) {
                        var document : DocumentSnapshot = it.result!!.documents[i]
                        var loadComment = Comment(document.getLong("type")!!, document.getString("email")!!,
                        document.getString("nickname")!!, document.getString("content")!!, document.getString("profileUri"), document.getLong("timestamp")!!)

                        loadAnswerComment(email, timestamp,loadComment.email + loadComment.timestamp, loadComment, db, callback)
                    }
                }

            }
    }

    fun loadAnswerComment(email : String, timestamp : Long, commentPath : String, loadComment : Comment,
                          db: FirebaseFirestore, callback : LongTaskCallback<Comment>){
        db.collection("feed")
            .document(email + timestamp)
            .collection("comment")
            .document(commentPath)
            .collection("comment_answer")
            .orderBy("timestamp")
            .get()
            .addOnCompleteListener {

                var list : MutableList<Comment> = mutableListOf()
                if(it.result != null) {
                    for(i in 0 until it.result!!.size()) {
                        var document : DocumentSnapshot = it.result!!.documents[i]
                        var commentItem = Comment(document.getLong("type")!!, document.getString("email")!!,
                            document.getString("nickname")!!, document.getString("content")!!, document.getString("profileUri"), document.getLong("timestamp")!!)
                        list.add(commentItem)
                    }

                    loadComment.setChildren(list.toList())
                    var result = Result.Success(loadComment)
                    callback.onResponse(result)
                }
            }


    }
}