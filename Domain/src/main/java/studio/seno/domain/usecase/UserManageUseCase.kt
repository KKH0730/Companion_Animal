package studio.seno.domain.usecase

import com.google.firebase.firestore.FirebaseFirestore
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.User
import java.lang.Exception

class UserManageUseCase {

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

    fun checkRemoteOverlapUser(email : String, db : FirebaseFirestore, callback : LongTaskCallback<Boolean>) {
        db.collection("user_list")
            .document("user_email")
            .get()
            .addOnSuccessListener {
                var findEmail = it.getString(email)
                if(findEmail == null) {
                    callback.onResponse(Result.Success(false))
                } else {
                    callback.onResponse(Result.Success(true))
                }
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
}