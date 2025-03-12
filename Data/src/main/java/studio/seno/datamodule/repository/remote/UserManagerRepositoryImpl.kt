package studio.seno.datamodule.repository.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import studio.seno.domain.repository.UserManagerRepository
import studio.seno.domain.model.User
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result
import javax.inject.Inject

class UserManagerRepositoryImpl @Inject constructor() : UserManagerRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun checkEnableLogin(
        email: String,
        password: String,
        callback: LongTaskCallback<Any>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful)
                        sendCallback(true, false, callback)
                    else
                        sendCallback(true, false, callback)

                }.addOnFailureListener{
                  sendCallback(it, true, callback)
                }
        }
    }

    override fun sendFindEmail(emailAddress: String, callback: LongTaskCallback<Any>) {
        CoroutineScope(Dispatchers.IO).launch {
            auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener {
                    if (it.isSuccessful)
                        sendCallback(true, false, callback)
                    else
                        sendCallback(true, false, callback)
                }.addOnFailureListener{
                    sendCallback(it, true, callback)
                }
        }
    }

    override fun registerUser(
        email: String,
        password: String,
        callback: LongTaskCallback<Any>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful)
                        sendCallback(true, false, callback)
                    else
                        sendCallback(true, false, callback)

                }.addOnFailureListener{
                    sendCallback(it, true, callback)
                }
        }
    }

    override fun setUserInfo(user: User) {
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("user")
                .document(user.email)
                .set(user)

            var map = HashMap<String, String>()
            map[user.email] = user.email
            db.collection("user_list")
                .document(user.email)
                .set(map)
        }
    }

    override fun getUserInfo(email: String, callback: LongTaskCallback<Any>) {
        CoroutineScope(Dispatchers.IO).launch {
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
                        sendCallback(user, false, callback)
                    } else {
                        getUserInfo(email, callback)
                    }

                }.addOnFailureListener{
                    sendCallback(it, true, callback)
                }
        }
    }

    override fun setNickname(content: String) {
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("user")
                .document(auth.currentUser?.email.toString())
                .update("nickname", content)
        }
    }

    override fun setToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("user")
                .document(auth.currentUser?.email.toString())
                .update("token", token)
        }
    }

    override fun checkOverlapUser(email : String, callback: LongTaskCallback<Any>) {
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("user_list")
                .document(email)
                .get()
                .addOnSuccessListener {
                    if(it.exists()) {
                        sendCallback(true, false, callback)
                    } else {
                        sendCallback(false, false, callback)
                    }
                }.addOnFailureListener {
                    sendCallback(it, true, callback)
                }
        }
    }

    override fun setProfileUri(profileUri: String) {
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("user")
                .document(auth.currentUser?.email.toString())
                .update("profileUri", profileUri)
        }
    }

    private fun sendCallback(any : Any, isError : Boolean, callback: LongTaskCallback<Any>?)  {
        CoroutineScope(Dispatchers.Main).launch {
            if(!isError)
                callback?.onResponse(Result.Success(any))
            else
                callback?.onResponse(Result.Error(any as java.lang.Exception))
        }
    }
}