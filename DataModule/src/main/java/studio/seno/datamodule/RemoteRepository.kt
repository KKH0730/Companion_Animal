package studio.seno.datamodule

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.model.*
import studio.seno.domain.usecase.remote.*

class RemoteRepository() {
    private val mAuth = FirebaseAuth.getInstance()
    private val mDB = FirebaseFirestore.getInstance()
    private val mStorageRef: StorageReference = FirebaseStorage.getInstance()
        .getReferenceFromUrl("gs://companion-animal-f0bfa.appspot.com/")
    private val mRTDB = FirebaseDatabase.getInstance()
    private val userUseCase = UserUseCase()
    private val feedUseCase = FeedUseCase()
    private val pagingModule = PagingModule()
    private val uploadUseCase = UploadUseCase()
    private val userManagerUseCase = RemoteUserUseCase()
    private val commentUseCase = CommentUseCase()
    private val followUseCase = FollowUseCase()
    private val notificationUseCase = NotificationUseCase()
    private val searchUseCase = SearchUseCase()
    private val chatUseCase = ChatUseCase()

    companion object{
        private var remoteRepository : RemoteRepository? = null

        fun getInstance() : RemoteRepository? {
            if(remoteRepository == null) {
                synchronized(LocalRepository::class.java) {
                    remoteRepository = RemoteRepository()
                }
            }
            return remoteRepository
        }

    }

    /**
     * User
     */
    fun requestCheckEnableLogin(email: String, password: String, callback : LongTaskCallback<Boolean>)  {
        userUseCase.checkEnableLogin(email, password, mAuth, callback)
    }

    fun requestRegisterUser(email: String, password: String, callback: LongTaskCallback<Boolean>) {
        userUseCase.registerUser(email, password,mAuth, callback)
    }

    fun requestSendFindEmail(emailAddress : String, callback : LongTaskCallback<Boolean>) {
        userUseCase.sendFindEmail(emailAddress, mAuth, callback)
    }

    //회원가입시 회원정보 서버에 저장
    fun uploadUserInfo(user: User) {
        userManagerUseCase.uploadRemoteUserInfo(user, mDB)
    }

    fun loadUserInfo(email: String, callback: LongTaskCallback<User>) {
        userManagerUseCase.loadRemoteUserInfo(email, mDB, callback)
    }

    //토큰 업데이트
    fun updateToken(token : String) {
        userManagerUseCase.updateToken(token, mAuth.currentUser?.email.toString(), mDB)
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


    //피드 작성후 서버에 업로드
    fun uploadFeed(feed: Feed, callback: LongTaskCallback<Feed>) {
        feedUseCase.uploadFeed(feed, mStorageRef, mDB, callback)
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
    fun requestUpdateHeart(feed : Feed, count: Long,flag : Boolean){
        feedUseCase.updateHeart(feed, count, mAuth.currentUser?.email.toString(), flag, mDB)
    }


    //북마크 상태 업데이트
    fun requestUpdateBookmark(feed : Feed, flag: Boolean) {
        feedUseCase.updateBookmark(feed, mAuth.currentUser?.email.toString(), flag, mDB)
    }

    /**
     * 이미지 업로드 및 이미지 로드
     */

    //profile image uri 로드
    fun loadRemoteProfileImage(email: String, callback: LongTaskCallback<String>){
        uploadUseCase.loadRemoteProfileImage(email, mStorageRef, callback)
    }


    /**
     * 팔로우 및 팔로워 이벤트
     */
    fun requestCheckFollow(targetEmail: String, callback: LongTaskCallback<Boolean>){
        followUseCase.checkFollow(targetEmail, mAuth.currentUser?.email.toString(), mDB, callback)
    }


    //팔로워 상태 업데이트
    fun requestUpdateFollower(targetEmail : String, flag: Boolean, myFollow : Follow, targetFollow: Follow) {
        followUseCase.updateFollower(targetEmail, FirebaseAuth.getInstance().currentUser?.email.toString(), flag, myFollow, targetFollow, mDB)
    }

    fun loadFollower(fieldName: String, callback: LongTaskCallback<List<Follow>>) {
        followUseCase.loadFollower(FirebaseAuth.getInstance().currentUser?.email.toString(), fieldName, mDB, callback)
    }


    /**
     * comment
     **/
    //피드 리스트에서 댓글 작성후 업로드
    fun uploadComment(
        targetEmail: String,
        targetTimestamp: Long,
        myComment: Comment
    ) {
        commentUseCase.uploadComment(
            targetEmail,
            targetTimestamp,
            myComment,
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
        myCommentAnswer: Comment
    ) {
        commentUseCase.uploadCommentAnswer(
            feedEmail,
            feedTimestamp,
            targetEmail,
            targetTimestamp,
            myCommentAnswer,
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
        commentUseCase.deleteComment(feedEmail, feedTimestamp, parentComment, childComment, mAuth.currentUser?.email.toString(), type, mDB)
    }

    /**
     * Notification
     */


    fun uploadNotificationInfo(notificationData : NotificationData){
        notificationUseCase.uploadNotificationInfo(mAuth.currentUser?.email.toString(), notificationData, mDB)
    }

    fun requestLoadNotification(callback : LongTaskCallback<List<NotificationData>>) {
        notificationUseCase.loadNotification(mAuth.currentUser?.email.toString(), mDB, callback)

    }

    fun requestUpdateCheckDot(notificationData : NotificationData){
        notificationUseCase.updateCheckDot(mAuth.currentUser?.email.toString(), notificationData, mDB)
    }

    //Notification 삭제
    fun requestDeleteNotification(notificationData : NotificationData, callback: LongTaskCallback<Boolean>){
        notificationUseCase.deleteNotification(mAuth.currentUser?.email.toString(), notificationData, mDB, callback)
    }
    /**
     * Search
     */

    //최근 검색 키워드 업로드
    fun requestUploadLastSearch(lastSearch: LastSearch) {
        searchUseCase.uploadLastSearch(mAuth.currentUser?.email.toString(), lastSearch, mDB)
    }

    //최근 검색 키워드 로드
    fun requestLoadLastSearch(callback : LongTaskCallback<List<LastSearch>>) {
        searchUseCase.loadLastSearch(mAuth.currentUser?.email.toString(), mDB, callback)
    }

    //최근 검색 삭제
    fun requestDeleteLastSearch(lastSearch: LastSearch){
        searchUseCase.deleteLastSearch(mAuth.currentUser?.email.toString(), lastSearch, mDB)
    }

    fun requestLoadFeedList(f1 : Boolean?, f2 : Boolean?, f3: Boolean?, keyword: String?, sort: String, myEmail: String?, recyclerView: RecyclerView, callback: LongTaskCallback<List<Feed>>){
        pagingModule.pagingFeed(f1, f2, f3, keyword, sort, myEmail, recyclerView, mDB, callback)
    }

    /**
     * Chat
     */

    fun requestLoadChatLog(myEmail: String, targetEmail: String, callback: LongTaskCallback<List<Chat>>){
        chatUseCase.loadChatLog(myEmail, targetEmail, mRTDB, callback)
    }

    fun requestAddChat(myEmail : String, targetEmail : String, chat : Chat){
        chatUseCase.addChat(myEmail, targetEmail, chat, mRTDB)
    }

    fun requestLoadChatList(myEmail : String, callback : LongTaskCallback<List<Chat>>){
        chatUseCase.loadChatList(myEmail, mRTDB, callback)
    }


    fun requestRemoveChatList(targetEmail : String, myEmail : String, chat : Chat){
        chatUseCase.removeChatList(targetEmail, myEmail, chat, mRTDB)
    }
    fun requestUpdateCheckDot(myEmail : String, targetEmail : String){
        chatUseCase.updateCheckDot(myEmail, targetEmail, mRTDB)
    }

    /**
     * 신고하기
     */

    fun reportFeed(feed : Feed, number : Int) {
        val map = mutableMapOf<String, Any>()
        map["path"] = feed.email + feed.timestamp
        map["report"] = number

        mDB.collection("report")
            .add(map)

    }

}