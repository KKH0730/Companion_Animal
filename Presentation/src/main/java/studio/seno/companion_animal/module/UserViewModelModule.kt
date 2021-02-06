package studio.seno.companion_animal.module

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import studio.seno.companion_animal.ui.user_manage.UserViewModel

class UserViewModelModule(viewModel: UserViewModel) {
    private val mAuth = FirebaseAuth.getInstance()
    private val memberViewModel = viewModel

    fun enableLogin(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful)
                    memberViewModel.setLoginLiveData(true)
                else
                    memberViewModel.setLoginLiveData(false)
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

    fun registerUser(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful)
                    memberViewModel.setRegisterLiveData(true)
                else
                    memberViewModel.setRegisterLiveData(false)
            }
    }
}