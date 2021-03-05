package studio.seno.datamodule.repository.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import studio.seno.domain.Repository.FollowRepository
import studio.seno.domain.model.Follow
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result

class FollowRepositoryImpl : FollowRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun checkFollow(
        targetEmail: String,
        callback: LongTaskCallback<Boolean>
    ) {
        db.collection("user")
            .document(auth.currentUser?.email.toString())
            .collection("following")
            .document(targetEmail)
            .get()
            .addOnCompleteListener {
                var result: Result<Boolean>
                if (targetEmail == it.result?.getString("email"))
                    result = Result.Success(true)
                else
                    result = Result.Success(false)
                callback.onResponse(result)

            }.addOnFailureListener {
                callback.onResponse(Result.Error(it))
            }
    }

    override fun getFollow(
        fieldName: String,
        callback: LongTaskCallback<List<Follow>>
    ) {
        db.collection("user")
            .document(auth.currentUser?.email.toString())
            .collection(fieldName)
            .get()
            .addOnCompleteListener {
                val list = mutableListOf<Follow>()
                var size: Int? = null

                if (it.result != null) {
                    size = it.result!!.size()
                    var document: List<DocumentSnapshot> = it.result!!.documents

                    for (element in document) {
                        var follower = Follow(
                            element.getString("email")!!,
                            element.getString("nickname")!!,
                            element.getString("profileUri")!!
                        )
                        list.add(follower)

                        if (list.size == size)
                            callback.onResponse(Result.Success(list.toList()))
                    }
                }
            }
    }

    override fun setFollower(
        targetEmail: String,
        flag: Boolean,
        myFollow: Follow,
        targetFollow: Follow
    ) {
        //flag = true 이면
        //User 콜렉션 :targetEmail의 follower 수 (+1)와 myemail의 following (+1) 수 업데이트
        //User 콜렉션 :targetEmail의 follower 컬렉션에 myemail 업로드, myemail의 following 컬렉션에 targetEmail 업로드
        //feed 콜렉션 :feed 게시물에 follower myEmail 업데이트

        //flag = false이면
        //User 콜렉션 :targetEmail의 follower 수(-1)와 myemail의 following(-1) 수 업데이트
        //User 콜렉션 :targetEmail의 follower 컬렉션에 myemail 삭제, myemail의 following 컬렉션에 targetEmail 삭제
        //feed 콜렉션 :feed 게시물에 follower myEmail 삭제
        val myEmail = auth.currentUser?.email.toString()
        if (flag) {
            followerNumberUpdate(targetEmail, "follower", true)
            followerNumberUpdate(myEmail, "following", true)

            followerStatusUpdate(targetEmail, myEmail, "follower", myFollow, true)
            followerStatusUpdate(myEmail, targetEmail, "following", targetFollow, true)
        } else {
            followerNumberUpdate(targetEmail, "follower", false)
            followerNumberUpdate(myEmail, "following", false)

            followerStatusUpdate(targetEmail, myEmail, "follower", myFollow, false)
            followerStatusUpdate(myEmail, targetEmail, "following", targetFollow, false)
        }
    }

    //개인 유저의 팔로워 및 팔로잉 수를 업데이트
    fun followerNumberUpdate(
        email: String,
        fieldName: String,
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
        follow: Follow,
        add: Boolean
    ) {
        if (add) {
            db.collection("user")
                .document(document1)
                .collection(fieldName)
                .document(document2)
                .set(follow)
        } else {
            db.collection("user")
                .document(document1)
                .collection(fieldName)
                .document(document2)
                .delete()
        }
    }
}