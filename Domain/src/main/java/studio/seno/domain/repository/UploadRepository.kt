package studio.seno.domain.repository

import android.net.Uri
import com.google.firebase.storage.StorageReference
import studio.seno.domain.util.LongTaskCallback

interface UploadRepository {
    fun setRemoteProfileImage(
        imageUri : Uri,
        callback: LongTaskCallback<Boolean>
    )

    fun getRemoteProfileImage(
        email : String,
        callback: LongTaskCallback<String>
    )


    fun setRemoteFeedImage(
        localUri : List<String>,
        path : String,
        storageRef: StorageReference,
        callback: LongTaskCallback<Boolean>
    )


    fun getRemoteFeedImage(
        path : String,
        storageRef: StorageReference,
        callback : LongTaskCallback<List<String>>
    )



    fun deleteRemoteFeedImage(
        email: String,
        timestamp : Long,
        storageRef: StorageReference,
        callback: LongTaskCallback<Boolean>
    )

}