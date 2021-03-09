package studio.seno.companion_animal.module

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.kakao.message.template.ButtonObject
import com.kakao.message.template.ContentObject
import com.kakao.message.template.FeedTemplate
import com.kakao.message.template.LinkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import studio.seno.companion_animal.R
import studio.seno.companion_animal.ui.MenuDialog
import studio.seno.companion_animal.ui.comment.CommentListViewModel
import studio.seno.companion_animal.ui.feed.*
import studio.seno.companion_animal.util.Constants
import studio.seno.datamodule.repository.local.LocalRepository
import studio.seno.datamodule.api.LinkShareApi
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result
import studio.seno.domain.model.Feed
import studio.seno.domain.model.User
import java.sql.Timestamp

class FeedModule (
    feedListViewModel: FeedListViewModel,
    commentViewModel : CommentListViewModel
) {
    private val mFeedListViewModel = feedListViewModel
    private val mCommentViewModel = commentViewModel
    private val currentUserEmail  = FirebaseAuth.getInstance().currentUser?.email.toString()


    /**
     * 북마크버튼
     */
    fun bookmarkButtonEvent(feed: Feed, bookmarkButton: ImageButton, feedAdapter : FeedListAdapter?){
        var map = feed.getBookmarkList().toMutableMap()
        if (map[currentUserEmail] != null) { //북마크 중인 상태에서 클릭
            updateBookmark(feed, false)
            map.remove(currentUserEmail)
            bookmarkButton.isSelected = false
        } else {
            updateBookmark(feed, true)
            map[currentUserEmail] = currentUserEmail
            bookmarkButton.isSelected = true
        }
        feed.setBookmarkList(map)
        feedAdapter?.notifyDataSetChanged()
    }

    private fun updateBookmark(feed: Feed, flag: Boolean) {
        mFeedListViewModel.updateBookmark(feed, flag) }

    /**
     * 좋아요버튼
     */

     fun heartButtonEvent(feed: Feed, heartCount: TextView, heartButton: ImageButton, feedAdapter : FeedListAdapter?){
        var map = feed.getHeartList().toMutableMap()
        var count = feed.getHeart()

        if (map[currentUserEmail] != null) {
            count--
            updateHeart(feed, count, false)
            map.remove(currentUserEmail)
            heartButton.isSelected = false
        } else {
            count++
            updateHeart(feed, count, true)
            map[currentUserEmail] = currentUserEmail
            heartButton.isSelected = true
        }

        feed.apply {
            setHeartList(map)
            setHeart(count)
        }
        heartCount.text = count.toString()
        feedAdapter?.notifyDataSetChanged()
     }
    private fun updateHeart(feed: Feed, count: Long, flag: Boolean) { mFeedListViewModel.updateHeart(feed, count, flag) }

    /**
     * 피드 메뉴 버튼
     */
    fun menuButtonEvent(feed: Feed, fm : FragmentManager){
        mFeedListViewModel.requestCheckFollow(
            feed.getEmail()!!,
            object : LongTaskCallback<Any> {
                override fun onResponse(result: Result<Any>) {
                    var dialog: MenuDialog? = null

                    if (result is Result.Success) {
                        if (result.data as Boolean)
                            dialog = MenuDialog.newInstance(feed.getEmail()!!, true)
                        else
                            dialog = MenuDialog.newInstance(feed.getEmail()!!, false)
                        dialog.show(fm, "feed")
                    } else if (result is Result.Error) {
                        Log.e("error", "follow check : ${result.exception}")
                    }
                }
            })
    }

    /**
     * 댓글 버튼
     */
    fun onCommentBtnClicked(
        feed: Feed,
        email : String,
        nickname : String,
        myProfileUri: String,
        commentEdit: EditText,
        commentCount: TextView,
        container: LinearLayout,
        lifecycleScope: LifecycleCoroutineScope
    ){
        //피드에 보여지는 댓글의 라이브 데이터 업데이트
        val textView = TextView(commentEdit.context)
        val commentContent = commentEdit.text.toString()
        container.apply{
            removeAllViews()
            SpannableStringBuilder(nickname).apply {

                TextModule().setTextColorBold(
                    this,
                    commentEdit.context,
                    R.color.black,
                    0,
                    nickname.length
                )
                append("  $commentContent")
                textView.text = this
            }
            addView(textView)
        }


        //댓글을 서버에 업로드
        mCommentViewModel.requestUploadComment(
            feed.getEmail()!!,
            feed.getTimestamp(),
            Constants.PARENT,
            email,
            nickname,
            commentEdit.text.toString(),
            Timestamp(System.currentTimeMillis()).time
        )

        //서버에 댓글 개수 업로드
        mCommentViewModel.requestUploadCommentCount(
            feed.getEmail()!!,
            feed.getTimestamp(),
            commentCount.text.toString().toLong(),
            true
        )


        //댓글수 업데이트
        commentCount.apply {
            var curCommentCount = Integer.valueOf(commentCount.text.toString())
            this.text = (curCommentCount + 1).toString()
        }

        commentEdit.setText("")
        commentEdit.hint = commentEdit.context.getString(R.string.comment_hint)
        CommonFunction.closeKeyboard(commentEdit.context, commentEdit)



        //댓글을 작성하면 notification 알림이 전송
        NotificationModule(commentEdit.context, nickname).sendNotification(feed.getEmail()!!, myProfileUri, commentContent, Timestamp(System.currentTimeMillis()).time, feed, lifecycleScope)



    }
    fun onDismiss(type : String, targetFeed : Feed?, activity: Activity, localRepository: LocalRepository, feedAdapter :FeedListAdapter?, lifecycleScope : LifecycleCoroutineScope, ){
        if(targetFeed != null) {
            if(type == "feed_modify") {
                val intent = Intent(activity, MakeFeedActivity::class.java)
                intent.putExtra("feed", targetFeed)
                intent.putExtra("mode", "modify")
                startActivityForResult(activity, intent, Constants.FEED_MODIFY_REQUEST, null)

            } else if(type == "feed_delete") {
                val intent = Intent(activity, MakeFeedActivity::class.java)
                intent.putExtra("feed", targetFeed)
                intent.putExtra("mode", "delete")
                startActivityForResult(activity, intent, Constants.FEED_DELETE_REQUEST, null)

            } else if(type == "follow") {
                localRepository.getUserInfo(lifecycleScope, object : LongTaskCallback<User> {
                    override fun onResponse(result: Result<User>) {
                        if(result is Result.Success) {
                            mFeedListViewModel.requestUpdateFollow(targetFeed.getEmail()!!,  targetFeed.getNickname()!!, targetFeed.getRemoteProfileUri(), true, result.data.nickname, result.data.profileUri)
                            localRepository.updateFollowing(lifecycleScope, true)

                        } else if(result is Result.Error) {
                            Log.e("error", "Homefragment follow error : ${result.exception}")
                        }
                    }
                })
            } else if(type == "unfollow") {
                localRepository.getUserInfo(lifecycleScope, object : LongTaskCallback<User> {
                    override fun onResponse(result: Result<User>) {
                        if(result is Result.Success) {
                            mFeedListViewModel.requestUpdateFollow(targetFeed.getEmail()!!,  targetFeed.getNickname()!!, targetFeed.getRemoteProfileUri(), false, result.data.nickname, result.data.profileUri)
                            localRepository.updateFollowing(lifecycleScope,false)

                        } else if(result is Result.Error) {
                            Log.e("error", "Homefragment follow error : ${result.exception}")
                        }
                    }
                })
            }
        }
    }

    fun sendShareLink(feed: Feed, context : Context, lifecycleScope: LifecycleCoroutineScope) {
        val params = FeedTemplate
            .newBuilder(
                ContentObject.newBuilder(
                    context.getString(R.string.kakao_title),
                    feed.getRemoteUri()[0],
                    LinkObject.newBuilder().setMobileWebUrl("https://www.naver.com")
                        .setAndroidExecutionParams("path=${feed.getEmail()}${feed.getTimestamp()}")
                        .build()
                )
                    .setDescrption(context.getString(R.string.kakao_description))
                    .build()
            )
            .addButton(
                ButtonObject(
                    context.getString(R.string.kakao_description2), LinkObject.newBuilder()
                        .setMobileWebUrl("https://www.naver.com")
                        .setAndroidExecutionParams("path=${feed.getEmail()}${feed.getTimestamp()}")
                        .build()
                )
            )
            .build()
        lifecycleScope.launch(Dispatchers.IO){
            LinkShareApi().sendShareLink(context, params, HashMap())
        }
    }
}