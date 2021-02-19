package studio.seno.datamodule

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.model.*
import studio.seno.domain.usecase.remote.*

class Repository() {
    private val mAuth = FirebaseAuth.getInstance()
    private val mDB = FirebaseFirestore.getInstance()
    private val mStorageRef: StorageReference = FirebaseStorage.getInstance()
        .getReferenceFromUrl("gs://companion-animal-f0bfa.appspot.com/")
    private val feedUseCase = FeedUseCase()
    private val uploadUseCase = UploadUseCase()
    private val userManagerUseCase = RemoteUserUseCase()
    private val commentUseCase = CommentUseCase()
    private val followUseCase = FollowUseCase()
    private val notificationUseCase = NotificationUseCase()
    private val searchUseCase = SearchUseCase()

    //회원가입시 회원정보 서버에 저장
    fun uploadUserInfo(user: User) {
        userManagerUseCase.uploadRemoteUserInfo(user, mDB)
    }

    fun loadUserInfo(email: String, callback: LongTaskCallback<User>) {
        userManagerUseCase.loadRemoteUserInfo(email, mDB, callback)
    }

    //토큰 업데이트
    fun updateToken(token : String, myEmail: String) {
        userManagerUseCase.updateToken(token, myEmail, mDB)
    }

    //닉네임 업데이트
    fun requestUpdateNickname(content: String) {
        userManagerUseCase.updateNickname(content, FirebaseAuth.getInstance().currentUser?.email.toString(), mDB)
    }

    //회원가입시 이메일 중복여부 확인
    fun checkOverlapEmail(email: String, callback: LongTaskCallback<Boolean>) {
        userManagerUseCase.checkRemoteOverlapUser(email, mDB, callback)
    }

    //회원가입시 기본이미지(no_profile) 업로드
    fun uploadInItProfileImage(imageUri: Uri, callback: LongTaskCallback<Boolean>) {
        uploadUseCase.uploadRemoteProfileImage(
            FirebaseAuth.getInstance().currentUser?.email.toString(),
            imageUri,
            mStorageRef,
            callback
        )
    }

    fun updateRemoteProfileUri(profileUri : String){
        userManagerUseCase.updateRemoteProfileUri(FirebaseAuth.getInstance().currentUser?.email.toString(), profileUri, mDB)
    }

    //profile image uri 로드
    fun loadRemoteProfileImage(callback: LongTaskCallback<String>){
        uploadUseCase.loadRemoteProfileImage(FirebaseAuth.getInstance().currentUser?.email.toString(), mStorageRef, callback)
    }


    //피드 작성후 서버에 업로드
    fun uploadFeed(context: Context, feed: Feed, lifecycleCoroutineScope: LifecycleCoroutineScope, callback: LongTaskCallback<Feed>) {
        feedUseCase.uploadFeed(context, feed, mDB, mStorageRef, lifecycleCoroutineScope, callback)
    }

    //피드 리스트 로드
    fun loadFeedList(callback: LongTaskCallback<List<Feed>>) {
        feedUseCase.loadFeedList( mDB, callback)
    }

    //하나의 피드 로드
    fun loadFeed(path: String, callback: LongTaskCallback<Feed>) {
        feedUseCase.loadFeed(path, mDB, callback)
    }

    //피드 삭제
    fun deleteFeed(feed: Feed, callback : LongTaskCallback<Boolean>){
        feedUseCase.deleteFeed(feed, mDB, mStorageRef, callback)
    }

    //좋아요 수 업데이트
    fun requestUpdateHeart(feed : Feed, count: Long, myEmail : String, flag : Boolean){
        feedUseCase.updateHeart(feed, count, myEmail, flag, mDB)
    }


    //북마크 상태 업데이트
    fun requestUpdateBookmark(feed : Feed, myEmail: String, flag: Boolean) {
        feedUseCase.updateBookmark(feed, myEmail, flag, mDB)
    }

    fun requestCheckFollow(targetFeed: Feed, myEmail: String, callback: LongTaskCallback<Boolean>){
        followUseCase.checkFollow(targetFeed, myEmail, mDB, callback)
    }


    //팔로워 상태 업데이트
    fun requestUpdateFollower(targetFeed : Feed, myEmail: String, flag: Boolean) {
        followUseCase.updateFollower(targetFeed, myEmail, flag, mDB)
    }


    /**
     * comment
     **/
    //피드 리스트에서 댓글 작성후 업로드
    fun uploadComment(
        targetEmail: String,
        targetTimestamp: Long,
        comment: Comment
    ) {
        commentUseCase.uploadComment(
            targetEmail,
            targetTimestamp,
            comment,
            mAuth,
            mDB,
            mStorageRef
        )
    }

    fun uploadCommentAnswer(
        feedEmail: String,
        feedTimestamp: Long,
        targetEmail: String,
        targetTimestamp: Long,
        commentAnswer: Comment
    ) {
        commentUseCase.uploadCommentAnswer(
            feedEmail,
            feedTimestamp,
            targetEmail,
            targetTimestamp,
            commentAnswer,
            mAuth,
            mDB,
            mStorageRef
        )
    }

    fun uploadCommentCount(targetEmail: String, targetTimestamp: Long, commentCount: Long, flag : Boolean) {
        commentUseCase.uploadCommentCount(targetEmail, targetTimestamp, commentCount, flag, mDB)
    }

    fun loadComment(email: String, timestamp: Long, callback: LongTaskCallback<List<Comment>>) {
        commentUseCase.loadComment(email, timestamp, mDB, callback)
    }

    fun deleteComment(
        feedEmail: String,
        feedTimestamp: Long,
        parentComment: Comment,
        childComment: Comment?,
        type: String
    ){
        commentUseCase.deleteComment(feedEmail, feedTimestamp, parentComment, childComment, type, mDB)
    }

    /**
     * Notification
     */


    fun uploadNotificationInfo(myEmail : String, notificationData : NotificationData){
        notificationUseCase.uploadNotificationInfo(myEmail, notificationData, mDB)
    }

    fun requestLoadNotification(myEmail : String, callback : LongTaskCallback<List<NotificationData>>) {
        notificationUseCase.loadNotification(myEmail, mDB, callback)

    }

    fun requestUpdateCheckDot(myEmail : String, notificationData : NotificationData){
        notificationUseCase.updateCheckDot(myEmail, notificationData, mDB)
    }

    //Notification 삭제
    fun requestDeleteNotification(myEmail: String, notificationData : NotificationData, callback: LongTaskCallback<Boolean>){
        notificationUseCase.deleteNotification(myEmail, notificationData, mDB, callback)
    }
    /**
     * Search
     */

    //최근 검색 키워드 업로드
    fun requestUploadLastSearch(myEmail : String, lastSearch: LastSearch) {
        searchUseCase.uploadLastSearch(myEmail, lastSearch, mDB)
    }

    //최근 검색 키워드 로드
    fun requestLoadLastSearch(myEmail: String, callback : LongTaskCallback<List<LastSearch>>) {
        searchUseCase.loadLastSearch(myEmail, mDB, callback)
    }

    //최근 검색 삭제
    fun requestDeleteLastSearch(myEmail : String, lastSearch: LastSearch){
        searchUseCase.deleteLastSearch(myEmail, lastSearch, mDB)
    }

    fun requestLoadFeedList(keyword: String?, recyclerView: RecyclerView, callback: LongTaskCallback<List<Feed>>){
        searchUseCase.searchFeed(keyword, recyclerView, mDB, callback)
    }



}