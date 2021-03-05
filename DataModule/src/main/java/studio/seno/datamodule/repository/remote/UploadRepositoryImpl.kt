package studio.seno.datamodule.repository.remote

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result
import studio.seno.domain.Repository.UploadRepository

class UploadRepositoryImpl : UploadRepository {
    private val storageRef: StorageReference = FirebaseStorage.getInstance()
        .getReferenceFromUrl("gs://companion-animal-f0bfa.appspot.com/")
    private val auth = FirebaseAuth.getInstance()

    override fun setRemoteProfileImage(
        imageUri: Uri,
        callback: LongTaskCallback<Boolean>
    ) {
        val email = auth.currentUser?.email.toString()
        val profileUri = "$email/profile/profileImage"

        storageRef.child(profileUri).putFile(imageUri)
            .addOnCompleteListener{
                callback.onResponse(Result.Success(true))
            }.addOnFailureListener{
                callback.onResponse(Result.Error(it))
            }
    }

    override fun getRemoteProfileImage(
        email: String,
        callback: LongTaskCallback<String>
    ) {
        val profileUri = "$email/profile/profileImage"

        storageRef.child(profileUri).downloadUrl
            .addOnCompleteListener {
                if(it.result != null) {
                    callback.onResponse(Result.Success(it.result.toString()))
                }
            }.addOnFailureListener {
                callback.onResponse(Result.Error(it))
            }
    }

    override fun setRemoteFeedImage(
        localUri: List<String>,
        path: String,
        storageRef: StorageReference,
        callback: LongTaskCallback<Boolean>
    ) {
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
                Log.d("error","upload Exception : ${it.message}")
            }
        }
    }

    override fun getRemoteFeedImage(
        path: String,
        storageRef: StorageReference,
        callback: LongTaskCallback<List<String>>
    ) {
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

    override fun deleteRemoteFeedImage(
        email: String,
        timestamp: Long,
        storageRef: StorageReference,
        callback: LongTaskCallback<Boolean>
    ) {
        var remoteImagePath = email + "/feed/" + timestamp + "/"

        storageRef.child(remoteImagePath).listAll().addOnCompleteListener {
            if (it.result != null) {
                val size = it.result!!.items.size
                var count = 0

                if(size == 0){
                    callback.onResponse(Result.Success(true))
                }

                for (element in it.result!!.items) {
                    count++
                    element.delete().addOnCompleteListener {
                        if(count == size) {
                            callback.onResponse(Result.Success(true))
                        }
                    }
                }
            }
        }
    }


}