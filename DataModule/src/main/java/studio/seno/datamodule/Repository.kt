package studio.seno.datamodule

import android.net.Uri
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.model.Feed
import studio.seno.domain.model.User
import studio.seno.domain.usecase.FeedUseCase
import studio.seno.domain.usecase.UploadUseCase
import studio.seno.domain.usecase.UserManageUseCase

class Repository() {
    private val mAuth = FirebaseAuth.getInstance()
    private val mDB = FirebaseFirestore.getInstance()
    private val mStorageRef: StorageReference = FirebaseStorage.getInstance()
        .getReferenceFromUrl("gs://companion-animal-f0bfa.appspot.com/")
    private val feedUseCase = FeedUseCase()
    private val uploadUseCase = UploadUseCase()
    private val userManagerUseCase = UserManageUseCase()

    //회원가입시 회원정보 서버에 저장
    fun uploadUserInfo(user: User) {
        userManagerUseCase.uploadRemoteUserInfo(user, mDB)
    }

    //회원가입시 이메일 중복여부 확인
    fun checkOverlapEmail(email : String, callback : LongTaskCallback<Boolean>) {
        userManagerUseCase.checkRemoteOverlapUser(email, mDB, callback)
    }

    //회원가입시 기본이미지(no_profile) 업로드
    fun uploadInItProfileImage(email: String, imageUri: Uri, callback: LongTaskCallback<Boolean>) {
        uploadUseCase.uploadRemoteProfileImage(
            email,
            imageUri,
            mStorageRef,
            callback
        )
    }

    //피드 작성후 서버에 업로드
    fun uploadFeed(feed: Feed, callback: LongTaskCallback<Boolean> ) {
        feedUseCase.uploadFeed(feed, mAuth, mDB, mStorageRef, callback)
    }

    fun loadFeedList(callback: LongTaskCallback<List<Feed>>){
        feedUseCase.loadFeedList(mAuth, mDB, mStorageRef, callback)
    }

}