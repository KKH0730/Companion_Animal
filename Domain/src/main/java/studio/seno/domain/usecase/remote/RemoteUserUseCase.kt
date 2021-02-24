package studio.seno.domain.usecase.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.User

class RemoteUserUseCase {
    fun uploadRemoteUserInfo(user: User, db : FirebaseFirestore) {
        db.collection("user")
            .document(user.email)
            .set(user)

        var map = HashMap<String, String>()
        map[user.email] = user.email
        db.collection("user_list")
            .document("user_email")
            .set(map)
    }

    fun loadRemoteUserInfo(email : String, db : FirebaseFirestore, callback: LongTaskCallback<User>) {
        db.collection("user")
            .document(email)
            .get()
            .addOnCompleteListener {
                val result = it.result
                if(result?.exists() == true) {
                    val user = User(result.getLong("id")!!, result.getString("email")!!, result.getString("nickname")!!,
                        result.getLong("follower")!!, result.getLong("following")!!, result.getLong("feedCount")!!,
                        result.getString("token")!!, result.getString("profileUri")!!
                    )
                    callback.onResponse(Result.Success(user))
                } else {
                    loadRemoteUserInfo(email, db, callback)
                }

            }.addOnFailureListener{
                callback.onResponse(Result.Error(it))
            }
    }

    fun updateNickname(content: String, myEmail: String, db: FirebaseFirestore) {
        db.collection("user")
            .document(myEmail)
            .update("nickname", content)
    }

    fun updateToken(token : String, myEmail : String, db: FirebaseFirestore) {
        db.collection("user")
            .document(myEmail)
            .update("token", token)
    }

    fun checkRemoteOverlapUser(email : String, db : FirebaseFirestore, callback : LongTaskCallback<Boolean>) {
        db.collection("user_list")
            .document("user_email")
            .get()
            .addOnSuccessListener {
                if(it.data != null) {
                    if(it?.data!![email] == null) {
                        callback.onResponse(Result.Success(false))
                    } else {
                        callback.onResponse(Result.Success(true))
                    }
                }

            }.addOnFailureListener {
                callback.onResponse(Result.Error(it))
            }
    }

    fun updateRemoteUserInfo(email : String, db : FirebaseFirestore, list : MutableList<String>) {

        for(i in 0 until list.size) {
            db.collection("user")
                .document(email)
                .get()
                .addOnSuccessListener {
                    var count : Long? = it.getLong(list[i])

                    if (count != null) {
                        db.collection("user")
                            .document(email)
                            .update(list[i], (count + 1))
                    }
                }
        }
    }

    fun updateRemoteProfileUri(myEmail : String, profileUri : String, db : FirebaseFirestore){
        db.collection("user")
            .document(myEmail)
            .update("profileUri", profileUri)

    }
}