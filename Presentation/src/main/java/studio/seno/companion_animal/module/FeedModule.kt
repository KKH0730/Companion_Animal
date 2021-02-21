package studio.seno.companion_animal.module

import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContentProviderCompat.requireContext
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.startActivity
import androidx.fragment.app.FragmentManager
import com.google.firebase.auth.FirebaseAuth
import okhttp3.ResponseBody
import studio.seno.companion_animal.R
import studio.seno.companion_animal.ui.MenuDialog
import studio.seno.companion_animal.ui.comment.CommentActivity
import studio.seno.companion_animal.ui.comment.CommentListViewModel
import studio.seno.companion_animal.ui.feed.FeedListAdapter
import studio.seno.companion_animal.ui.feed.FeedListViewModel
import studio.seno.companion_animal.ui.main_ui.MainViewModel
import studio.seno.companion_animal.util.Constants
import studio.seno.datamodule.api.ApiClient
import studio.seno.datamodule.api.ApiInterface
import studio.seno.datamodule.model.NotificationModel
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Feed
import studio.seno.domain.model.NotificationData
import studio.seno.domain.model.User
import studio.seno.domain.util.PrefereceManager
import java.sql.Timestamp

class FeedModule(
    feedListViewModel: FeedListViewModel,
    commentViewModel : CommentListViewModel,
    mainViewModel : MainViewModel
) {
    private val mFeedListViewModel = feedListViewModel
    private val mCommentViewModel = commentViewModel
    private val mMainViewModel  = mainViewModel
    private val currentUserEmail  = FirebaseAuth.getInstance().currentUser?.email.toString()


    /**
     * 북마크버튼
     */
    fun bookmarkButtonEvent(feed: Feed, bookmarkButton: ImageButton, feedAdapter : FeedListAdapter?){
        var map = feed.bookmarkList.toMutableMap()
        if (map[currentUserEmail] != null) { //북마크 중인 상태에서 클릭
            updateBookmark(feed, false)
            map.remove(currentUserEmail)
            bookmarkButton.isSelected = false
        } else {
            updateBookmark(feed, true)
            map[currentUserEmail] = currentUserEmail
            bookmarkButton.isSelected = true
        }
        feed.bookmarkList = map
        feedAdapter?.notifyDataSetChanged()
    }

    private fun updateBookmark(feed: Feed, flag: Boolean) {
        mFeedListViewModel.requestUpdateBookmark(feed, flag) }

    /**
     * 좋아요버튼
     */

     fun heartButtonEvent(feed: Feed, heartCount: TextView, heartButton: ImageButton, feedAdapter : FeedListAdapter?){
        var map = feed.heartList.toMutableMap()
        var count = feed.heart

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
            heartList = map
            heart = count
        }
        heartCount.text = count.toString()
        feedAdapter?.notifyDataSetChanged()
     }
    private fun updateHeart(feed: Feed, count: Long, flag: Boolean) { mFeedListViewModel.requestUpdateHeart(feed, count, flag) }

    /**
     * 피드 메뉴 버튼
     */
    fun menuButtonEvent(feed: Feed, fm : FragmentManager){
        mFeedListViewModel.requestCheckFollow(
            feed.email,
            object : LongTaskCallback<Boolean> {
                override fun onResponse(result: Result<Boolean>) {
                    var dialog: MenuDialog? = null

                    if (result is Result.Success) {
                        if (result.data)
                            dialog = MenuDialog.newInstance(feed.email, true)
                        else
                            dialog = MenuDialog.newInstance(feed.email, false)
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
        commentEdit: EditText,
        commentCount: TextView,
        container: LinearLayout
    ){
        //피드에 보여지는 댓글의 라이브 데이터 업데이트
        //model.setFeedCommentLiveData(commentEdit.text.toString())
        val textView = TextView(commentEdit.context)
        val nickname = PrefereceManager.getString(commentEdit.context, "nickName")
        val commentContent = commentEdit.text.toString()
        container.apply{
            removeAllViews()
            SpannableStringBuilder(nickname).apply {

                TextModule().setTextColorBold(
                    this,
                    commentEdit.context,
                    R.color.black,
                    0,
                    nickname!!.length
                )
                append("  $commentContent")
                textView.text = this
            }
            addView(textView)
        }


        //댓글을 서버에 업로드
        mCommentViewModel.requestUploadComment(
            feed.email,
            feed.timestamp,
            Constants.PARENT,
            FirebaseAuth.getInstance().currentUser?.email.toString(),
            PrefereceManager.getString(
                commentEdit.context,
                "nickName"
            )!!,
            commentEdit.text.toString(),
            Timestamp(System.currentTimeMillis()).time
        )

        //서버에 댓글 개수 업로드
        mCommentViewModel.requestUploadCommentCount(
            feed.email,
            feed.timestamp,
            commentCount.text.toString().toLong(),
            true
        )

        //댓글수 업데이트
        commentCount.apply {
            var curCommentCount = Integer.valueOf(commentCount.text.toString())
            text = (curCommentCount + 1).toString()
        }

        commentEdit.setText("")
        commentEdit.hint = commentEdit.context.getString(R.string.comment_hint)
        CommonFunction.closeKeyboard(commentEdit.context, commentEdit)



        //댓글을 작성하면 notification 알림이 전송
        NotificationModule(commentEdit.context, mMainViewModel).sendNotification(feed.email, commentContent, Timestamp(System.currentTimeMillis()).time, feed)
        /*
        mMainViewModel.requestUserData(feed.email, object : LongTaskCallback<User> {
            override fun onResponse(result: Result<User>) {
                if(result is Result.Success) {

                    val timestamp = Timestamp(System.currentTimeMillis()).time
                    val notificationModel = NotificationModel(
                        result.data.token,
                        NotificationData(
                            nickname!!,
                            commentContent,
                            timestamp,
                            feed.email + timestamp,
                            feed.email + feed.timestamp,
                            true
                        )
                    )

                    var apiService = ApiClient.getClient().create(ApiInterface::class.java)
                    var responseBodyCall: retrofit2.Call<ResponseBody> = apiService.sendNotification(notificationModel)
                    responseBodyCall.enqueue(object : retrofit2.Callback<ResponseBody> {
                        override fun onResponse(
                            call: retrofit2.Call<ResponseBody>,
                            response: retrofit2.Response<ResponseBody>
                        ) {

                        }

                        override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {

                        }

                    })
                }
            }
        })
         */
    }

}