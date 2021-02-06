package studio.seno.domain.usecase

import android.util.Log
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
import studio.seno.domain.model.Feed
import studio.seno.domain.model.User

class FeedUseCase {
    private val userMangerUseCase = UserManageUseCase()

    fun uploadFeed(
        feed: Feed, auth: FirebaseAuth, mDB: FirebaseFirestore,
        storageRef: StorageReference, callback: LongTaskCallback<Boolean>
    ) {
        var profileUri = auth.currentUser?.email + "/profile/profileImage"
        var localUri = auth.currentUser?.email + "/feed/" + feed.timestamp + "/"

        //Feed 이미지 업로드
        UploadUseCase().uploadRemoteFeedImage(
            feed,
            storageRef,
            localUri,
            object : LongTaskCallback<Boolean> {
                override fun onResponse(result: Result<Boolean>) {

                    //프로필 이미지 객체 저장
                    storageRef.child(profileUri)
                        .downloadUrl
                        .addOnCompleteListener { it ->
                            feed.remoteProfileUri = it.toString()


                            storageRef.child(localUri).listAll().addOnCompleteListener { it2 ->
                                var listResult = it2.result?.items!!


                                //Feed 이미지 로드 후 객체에 저장
                                UploadUseCase().loadRemoteFeedImage(
                                    listResult,
                                    object : LongTaskCallback<MutableList<String>> {
                                        override fun onResponse(result: Result<MutableList<String>>) {
                                            var res = (result as Result.Success).data
                                            feed.remoteUri = res

                                            //db에 객체 데이터 저장
                                            mDB.collection("Feed")
                                                .document(feed.email + feed.timestamp)
                                                .set(feed)
                                                .addOnCompleteListener {
                                                    var result: Result<Boolean>? = null
                                                    if (it.isSuccessful)
                                                        result = Result.Success(true)
                                                    else
                                                        result = Result.Success(false)
                                                    callback.onResponse(result)
                                                }.addOnFailureListener {
                                                    Log.e("db", "feed_save_errer ${it.message}")
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
        db.collection("Feed")
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
}