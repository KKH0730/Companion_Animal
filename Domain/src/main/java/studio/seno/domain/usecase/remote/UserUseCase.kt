package studio.seno.domain.usecase.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result

class UserUseCase {

    fun checkEnableLogin(email: String, password: String, mAuth: FirebaseAuth, callback : LongTaskCallback<Boolean>)  {
        mAuth.signInWithEmailAndPassword(email, password)
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

    fun sendFindEmail(emailAddress: String, mAuth: FirebaseAuth, calllback : LongTaskCallback<Boolean>) {
        mAuth.sendPasswordResetEmail(emailAddress)
            .addOnCompleteListener {
                if (it.isSuccessful)
                    calllback.onResponse(Result.Success(true))
                else
                    calllback.onResponse(Result.Success(false))
            }
    }

    fun registerUser(email: String, password: String, mAuth: FirebaseAuth, callback: LongTaskCallback<Boolean>) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful)
                    callback.onResponse(Result.Success(true))
                else
                    callback.onResponse(Result.Success(false))

            }.addOnFailureListener{
                Log.e("register fail", "error : " + it.message)
            }
    }
}