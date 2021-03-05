package studio.seno.datamodule.repository.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import studio.seno.domain.Repository.UserManagerRepository
import studio.seno.domain.model.User
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result

class UserManagerRepositoryImpl : UserManagerRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun checkEnableLogin(
        email: String,
        password: String,
        callback: LongTaskCallback<Boolean>
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback.onResponse(Result.Success(true))
                } else {
                    callback.onResponse(Result.Success(false))
                }
            }.addOnFailureListener{
                Log.e("login_error", "error " + it.message)
            }
    }

    override fun sendFindEmail(emailAddress: String, callback: LongTaskCallback<Boolean>) {
        auth.sendPasswordResetEmail(emailAddress)
            .addOnCompleteListener {
                if (it.isSuccessful)
                    callback.onResponse(Result.Success(true))
                else
                    callback.onResponse(Result.Success(false))
            }
    }

    override fun registerUser(
        email: String,
        password: String,
        callback: LongTaskCallback<Boolean>
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful)
                    callback.onResponse(Result.Success(true))
                else
                    callback.onResponse(Result.Success(false))

            }.addOnFailureListener{
                Log.e("register fail", "error : " + it.message)
            }
    }

    override fun setUserInfo(user: User) {
        db.collection("user")
            .document(user.email)
            .set(user)

        var map = HashMap<String, String>()
        map[user.email] = user.email
        db.collection("user_list")
            .document("user_email")
            .set(map)
    }

    override fun getUserInfo(email: String, callback: LongTaskCallback<User>) {
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
                    getUserInfo(email, callback)
                }

            }.addOnFailureListener{
                callback.onResponse(Result.Error(it))
            }
    }

    override fun setNickname(content: String) {
        db.collection("user")
            .document(auth.currentUser?.email.toString())
            .update("nickname", content)
    }

    override fun setToken(token: String) {
        db.collection("user")
            .document(auth.currentUser?.email.toString())
            .update("token", token)
    }

    override fun checkOverlapUser(email : String, callback: LongTaskCallback<Boolean>) {
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

    override fun setProfileUri(profileUri: String) {
        db.collection("user")
            .document(auth.currentUser?.email.toString())
            .update("profileUri", profileUri)
    }
}