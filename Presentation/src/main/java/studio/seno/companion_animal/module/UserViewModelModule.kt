package studio.seno.companion_animal.module

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import studio.seno.companion_animal.ui.user_manage.UserViewModel
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result

class UserViewModelModule(viewModel: UserViewModel) {
    private val mAuth = FirebaseAuth.getInstance()
    private val memberViewModel = viewModel

    fun enableLogin(email: String, password: String, callback : LongTaskCallback<Boolean>)  {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback.onResponse(Result.Success(true))
                    //memberViewModel.setLoginLiveData(true)
                } else {
                    callback.onResponse(Result.Success(false))
                    //memberViewModel.setLoginLiveData(false)
                }
            }.addOnFailureListener{
                Log.e("login_error", "error " + it.message)
            }

    }

    fun sendFindEmail(emailAddress: String) {
        mAuth.sendPasswordResetEmail(emailAddress)
            .addOnCompleteListener {
                if (it.isSuccessful)
                    memberViewModel.setFindPasswordListData(true)
                else
                    memberViewModel.setFindPasswordListData(false)
            }
    }

    fun registerUser(email: String, password: String, callback: LongTaskCallback<Boolean>) {
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