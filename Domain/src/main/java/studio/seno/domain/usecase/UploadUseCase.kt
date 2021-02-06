package studio.seno.domain.usecase

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Feed
import java.util.*

class UploadUseCase {

    //회원가입 페이지에서 프로필 이미지(no_image.png) 서버에 저장
    fun uploadRemoteProfileImage(email : String, imageUri : Uri, storageRef: StorageReference
                                 , callback: LongTaskCallback<Boolean>){
        var profileUri = "$email/profile/profileImage"

            storageRef.child(profileUri).putFile(imageUri)
                .addOnCompleteListener{
                    callback.onResponse(Result.Success(true))
                }.addOnCompleteListener{
                    callback.onResponse(Result.Success(false))
                }
    }

    fun uploadRemoteFeedImage(feed : Feed, storageRef: StorageReference, path : String, callback: LongTaskCallback<Boolean>){
        for (i in 0..feed.localUri.size - 1) {
            Log.d("hi", "i : $i")
            storageRef.child(path + i).putFile(Uri.parse(feed.localUri[i]))
                .addOnCompleteListener {
                    if(i == feed.localUri.size - 1){
                        Log.d("hi", "callback")
                        callback.onResponse(Result.Success(true))
                    }
                }
        }
    }


    fun loadRemoteFeedImage(listResult : MutableList<StorageReference>, callback : LongTaskCallback<MutableList<String>>)  {
        var list = mutableListOf<String>()

        for(i in 0..(listResult.size - 1)) {
            listResult[i].downloadUrl.addOnCompleteListener {
                list.add(it.result.toString())
            }
        }
        val timer = Timer()
        var tt = object : TimerTask(){
            override fun run() {
                if(list.size == listResult.size) {
                    callback.onResponse(Result.Success(list))
                    timer.cancel()
                }
            }
        }
        timer.schedule(tt, 1000)
    }
}