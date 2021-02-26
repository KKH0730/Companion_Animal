package studio.seno.domain.usecase.remote

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.component1
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
                }.addOnFailureListener{
                    callback.onResponse(Result.Error(it))
                }
    }

    /*
    fun uploadRemoteFeedImage(feed : Feed, storageRef: StorageReference, path : String, callback: LongTaskCallback<Boolean>){
        val tempList = mutableListOf<Uri>()
        val size = feed.localUri.size

        for (i in 0 until feed.localUri.size) {
            storageRef.child(path + i).putFile(Uri.parse(feed.localUri[i]))
                .addOnCompleteListener {
                    it.result?.uploadSessionUri?.let { it1 -> tempList.add(it1) }

                    if(tempList.size == size)
                        callback.onResponse(Result.Success(true))

                }.addOnFailureListener {
                    Log.d("hi","upload Exception : ${it.message}")
                }
        }
    }
     */

    fun uploadRemoteFeedImage(localUri : List<String>, path : String, storageRef: StorageReference, callback: LongTaskCallback<Boolean>){
        var count = 0
        for (i in 0 until localUri.size) {
            if (!localUri[i].contains("firebase"))
                count++
        }
        if(count == 0) {
            callback.onResponse(Result.Success(true))
            return
        }

        val size = count
        count = 0
        for (i in 0 until localUri.size) {
            if(localUri[i].contains("firebase"))
                continue

            storageRef.child(path + i).putFile(Uri.parse(localUri[i])).addOnCompleteListener{
                count++

                if(count == size)
                    callback.onResponse(Result.Success(true))

            }.addOnFailureListener {
                Log.d("hi","upload Exception : ${it.message}")
            }
        }
    }

    fun loadRemoteProfileImage(email : String, storageRef: StorageReference, callback: LongTaskCallback<String>){
        var profileUri = "$email/profile/profileImage"

        storageRef.child(profileUri).downloadUrl
            .addOnCompleteListener {
            if(it.result != null) {
                callback.onResponse(Result.Success(it.result.toString()))
            }
        }.addOnFailureListener {
                callback.onResponse(Result.Error(it))
        }
    }

/*
    fun loadRemoteFeedImage(listResult : MutableList<StorageReference>, callback : LongTaskCallback<MutableList<String>>)  {
        var list = mutableListOf<String>()

        for(i in 0 until listResult.size) {
            listResult[i].downloadUrl.addOnCompleteListener {
                list.add(it.result.toString())

                if(list.size == listResult.size) {
                    callback.onResponse(Result.Success(list))
                    timer.cancel()
                }
            }
        }
    }
 */

    fun loadRemoteFeedImage(path : String, uriSize : Int, storageRef: StorageReference, callback : LongTaskCallback<List<String>>)  {
        storageRef.child(path).listAll().addOnCompleteListener {
            var listResult = it.result?.items

            var list = mutableListOf<String>()

            for(i in 0 until listResult?.size!!) {
                listResult[i].downloadUrl.addOnCompleteListener {
                    list.add(it.result.toString())

                    if(list.size == listResult.size) {
                        callback.onResponse(Result.Success(list))
                    }
                }
            }
        }
    }

    fun deleteRemoteFeedImage(email: String, timestamp : Long, toRemoveUri : List<Int>, mode : String, storageRef: StorageReference, callback: LongTaskCallback<Boolean>){
        var remoteImagePath = email + "/feed/" + timestamp + "/"


        if(mode != "modify" || toRemoveUri.size == 0) {
            callback.onResponse(Result.Success(true))
            return
        }
        Log.d("hi", " size -> ${toRemoveUri.size}")
        for(element in toRemoveUri){
            Log.d("hi", " delete -> $element")
        }

        var size = toRemoveUri.size
        var count = 0
        for(element in toRemoveUri) {
            storageRef.child(remoteImagePath + element).delete().addOnCompleteListener {
                count++

                Log.d("hi", " real -> $element")
                Log.d("hi", " count -> $count")
                if(count == size) {
                    Log.d("hi", "exit")
                    callback.onResponse(Result.Success(true))
                }
            }
        }
    }
}