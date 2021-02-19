package studio.seno.domain.usecase.remote

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Feed

class FollowUseCase {

    //대상을 팔로우 하고 있는지 확인한다.
    fun checkFollow(
        targetFeed: Feed,
        myEmail: String,
        db: FirebaseFirestore,
        callback: LongTaskCallback<Boolean>
    ){
        db.collection("user")
            .document(myEmail)
            .collection("following")
            .document(targetFeed.email)
            .get()
            .addOnCompleteListener {
                var result : Result<Boolean>
                if(targetFeed.email == it.result?.getString("email"))
                    result = Result.Success(true)
                else
                    result = Result.Success(false)
                callback.onResponse(result)

            }.addOnFailureListener {
                callback.onResponse(Result.Error(it))
            }
    }


    //팔로워를 불러온다.
    fun loadFollower(
        feed: Feed,
        db: FirebaseFirestore,
        callback: LongTaskCallback<MutableMap<String, String>>
    ) {
        db.collection("user")
            .document(feed.email!!)
            .collection("follower")
            .get()
            .addOnCompleteListener {
                val map = mutableMapOf<String, String>()
                var size: Int? = null
                if (it.result != null) {
                    size = it.result!!.size()
                    for (i in 0 until it.result!!.size()) {
                        var document: List<DocumentSnapshot> = it.result!!.documents
                        for (element in document) {
                            var follower = element.getString("email")
                            if (follower != null) {
                                map[follower] = follower
                            }
                        }
                    }

                    if (map.size == size)
                        callback.onResponse(Result.Success(map))
                }
            }
    }

    //followerNumberUpdate()메소드와 followerStatusUpdate()메소드를 이용해 팔로우 및 팔로워 업데이트
    fun updateFollower(targetFeed: Feed, myEmail: String, flag: Boolean, db: FirebaseFirestore) {
        //flag = true 이면
        //User 콜렉션 :targetEmail의 follower 수 (+1)와 myemail의 following (+1) 수 업데이트
        //User 콜렉션 :targetEmail의 follower 컬렉션에 myemail 업로드, myemail의 following 컬렉션에 targetEmail 업로드
        //feed 콜렉션 :feed 게시물에 follower myEmail 업데이트

        //flag = false이면
        //User 콜렉션 :targetEmail의 follower 수(-1)와 myemail의 following(-1) 수 업데이트
        //User 콜렉션 :targetEmail의 follower 컬렉션에 myemail 삭제, myemail의 following 컬렉션에 targetEmail 삭제
        //feed 콜렉션 :feed 게시물에 follower myEmail 삭제
        if (flag) {
            followerNumberUpdate(targetFeed.email!!, "follower", db, true)
            followerNumberUpdate(myEmail, "following", db, true)

            followerStatusUpdate(targetFeed.email!!, myEmail, "follower", myEmail, db, true)
            followerStatusUpdate(
                myEmail,
                targetFeed.email!!, "following", targetFeed.email!!, db, true
            )
        } else {
            followerNumberUpdate(targetFeed.email!!, "follower", db, false)
            followerNumberUpdate(myEmail, "following", db, false)

            followerStatusUpdate(targetFeed.email!!, myEmail, "follower", myEmail, db, false)
            followerStatusUpdate(
                myEmail, targetFeed.email!!, "following",
                targetFeed.email!!, db, false
            )
        }
    }

    //개인 유저의 팔로워 및 팔로잉 수를 업데이트
    fun followerNumberUpdate(
        email: String,
        fieldName: String,
        db: FirebaseFirestore,
        add: Boolean
    ) {
        db.collection("user")
            .document(email)
            .get()
            .addOnCompleteListener {
                if (it.result != null) {
                    var count = it.result!!.getLong(fieldName)

                    if (add) {
                        db.collection("user").document(email).update(fieldName, count!! + 1L)
                    } else {
                        db.collection("user").document(email).update(fieldName, count!! - 1L)
                    }
                }

            }
    }

    //팔로우 버튼 눌렀을때, 게시자의 팔로워와 누른 사람의 팔로잉 상태를 업데이트
    fun followerStatusUpdate(
        document1: String,
        document2: String,
        fieldName: String,
        updateEmail: String,
        db: FirebaseFirestore,
        add: Boolean
    ) {
        if (add) {
            var map = mutableMapOf<String, String>()
            map["email"] = updateEmail

            db.collection("user")
                .document(document1)
                .collection(fieldName)
                .document(document2)
                .set(map)
        } else {
            Log.d("hi","document1 : $document1 , docuent2 : $document2")
            db.collection("user")
                .document(document1)
                .collection(fieldName)
                .document(document2)
                .delete()
        }
    }
}