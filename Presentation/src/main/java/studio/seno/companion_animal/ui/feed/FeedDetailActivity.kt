package studio.seno.companion_animal.ui.feed


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import dagger.hilt.android.AndroidEntryPoint
import studio.seno.companion_animal.base.CustomToast
import studio.seno.companion_animal.MainActivity
import studio.seno.companion_animal.R
import studio.seno.companion_animal.ui.ReportActivity
import studio.seno.companion_animal.databinding.ActivityFeedDetailBinding
import studio.seno.companion_animal.extension.startActivity
import studio.seno.companion_animal.module.CommentModule
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.module.FeedModule
import studio.seno.companion_animal.module.NotificationModule
import studio.seno.companion_animal.ui.MenuDialog
import studio.seno.companion_animal.ui.comment.CommentAdapter
import studio.seno.companion_animal.ui.comment.CommentListViewModel
import studio.seno.companion_animal.ui.comment.OnCommentEventListener
import studio.seno.companion_animal.util.Constants
import studio.seno.datamodule.repository.local.LocalRepository
import studio.seno.domain.model.Comment
import studio.seno.domain.model.Feed
import studio.seno.domain.model.User
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.PreferenceManager
import studio.seno.domain.util.Result
import java.sql.Timestamp

@AndroidEntryPoint
class FeedDetailActivity : AppCompatActivity(), View.OnClickListener,
    DialogInterface.OnDismissListener {
    private var feed: Feed? = null
    private var binding: ActivityFeedDetailBinding? = null
    private lateinit var feedViewModel: FeedViewModel
    private val feedListViewModel: FeedListViewModel by viewModels()
    private val commentViewModel: CommentListViewModel by viewModels()
    private val commentListViewModel: CommentListViewModel by viewModels()
    private val feedModule: FeedModule by lazy { FeedModule(feedListViewModel, commentViewModel) }
    private lateinit var notificationModule : NotificationModule
    private var curComment: Comment? = null
    private var answerComment: Comment? = null
    private var answerPosition = 0
    private var commentPosition = 0
    private var commentAdapter : CommentAdapter? = CommentAdapter()
    private var answerMode = false
    private var modifyMode = false
    private lateinit var commentModule: CommentModule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_feed_detail)

        init()

        LocalRepository.getInstance(this)!!.getUserInfo(lifecycleScope, object :
            LongTaskCallback<User> {
            override fun onResponse(result: Result<User>) {
                if (result is Result.Success) {
                    notificationModule = NotificationModule(
                        applicationContext,
                        result.data.nickname
                    )

                    binding!!.feedLayout.bookmarkBtn2.isSelected = feed!!.getBookmarkList()[result.data.email] != null

                    commentModule = CommentModule(
                        commentListViewModel, feed!!, result.data.email,
                        result.data.nickname, applicationContext, commentAdapter!!
                    )
                    commentItemEvent()
                    observe()
                }
            }
        })
    }

    fun init() {
        feedViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FeedViewModel(
                    lifecycle,
                    supportFragmentManager,
                    binding!!.feedLayout.indicator,
                    lifecycleScope
                ) as T
            }
        }).get(FeedViewModel::class.java)

        binding!!.feedLayout.lifecycleOwner = this
        binding!!.feedLayout.model = feedViewModel

        binding!!.commentLayout.lifecycleOwner = this
        binding!!.commentLayout.model = commentListViewModel
        binding!!.commentLayout.commentRecyclerView.adapter = commentAdapter

        if (intent.getParcelableExtra<Feed>("feed") == null)
            finish()
        else {
            feed = intent.getParcelableExtra("feed")
            feedViewModel.setFeedLiveData(feed!!)
            binding!!.feedLayout.commentShow.visibility = View.GONE
            commentListViewModel.requestLoadComment(feed!!.getEmail()!!, feed!!.getTimestamp())
        }

        binding!!.header.findViewById<TextView>(R.id.title2).text = feed!!.getNickname()
        binding!!.header.findViewById<ImageButton>(R.id.back_btn).setOnClickListener(this)
        binding!!.commentLayout.commentContainer.visibility = View.GONE
        binding!!.commentLayout.showCommentBar.visibility = View.GONE
        binding!!.commentLayout.header.visibility = View.GONE
        binding!!.feedLayout.detailBtn.visibility = View.GONE
        binding!!.feedLayout.profileHeader.visibility = View.GONE
        binding!!.feedLayout.feedMenu2.visibility = View.VISIBLE
        binding!!.feedLayout.profileBtn.visibility = View.VISIBLE
        binding!!.feedLayout.profileBtn.setOnClickListener(this)
        binding!!.feedLayout.bookmarkBtn2.visibility = View.VISIBLE
        binding!!.feedLayout.bookmarkBtn2.setOnClickListener(this)
        binding!!.feedLayout.heartBtn.setOnClickListener(this)
        binding!!.feedLayout.feedMenu2.setOnClickListener(this)
        binding!!.feedLayout.commentBtn.setOnClickListener(this)
        binding!!.feedLayout.imageBtn.setOnClickListener(this)
        binding!!.feedLayout.shareBtn.setOnClickListener(this)
        binding!!.feedLayout.comment.addTextChangedListener(textWatcher)
    }


    fun commentItemEvent() {
        commentAdapter!!.setOnEventListener(object : OnCommentEventListener {
            override fun onReadAnswerClicked(readAnswer: Button, targetComment: Comment) {
                if (targetComment.getChildren()!!.isNotEmpty())
                    commentModule.showComment(readAnswer, targetComment)
                else {
                    commentModule.hideComment(readAnswer, targetComment)
                }
            }

            override fun onWriteAnswerClicked(targetComment: Comment, position: Int) {
                curComment = targetComment
                commentPosition = position
                binding!!.feedLayout.comment.requestFocus()
                CommonFunction.getInstance()!!.showKeyboard(applicationContext)

                commentModule.setHint(
                    binding!!.feedLayout.comment,
                    binding!!.commentLayout.modeTitle,
                    1
                )
                answerMode = true
                binding!!.commentLayout.modeLayout.visibility = View.VISIBLE
            }

            override fun onMenuClicked(comment: Comment, position: Int) {
                var menuDialog = MenuDialog.newInstance(comment.email, false)

                if (comment.type == Constants.PARENT) {
                    menuDialog.show(supportFragmentManager, "comment")
                    answerMode = false
                    curComment = comment
                    commentPosition = position
                } else {
                    menuDialog.show(supportFragmentManager, "comment_answer")
                    answerMode = true
                    answerComment = comment
                    answerPosition = position
                }
            }
        })
    }


    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (binding!!.feedLayout.comment.text.isEmpty())
                binding!!.feedLayout.commentBtn.visibility = View.INVISIBLE
            else
                binding!!.feedLayout.commentBtn.visibility = View.VISIBLE
        }
    }

    private fun observe() {
        commentListViewModel.getCommentListLiveData().observe(this, {
            commentAdapter!!.submitList(it)
        })
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.back_btn) {
            setIntent()
            finish()
        } else if (v?.id == R.id.bookmark_btn2) {
            feedModule.bookmarkButtonEvent(feed!!, binding!!.feedLayout.bookmarkBtn2, null)

        } else if (v?.id == R.id.heart_btn) {
            feedModule.heartButtonEvent(
                feed!!,
                binding!!.feedLayout.heartCount,
                binding!!.feedLayout.heartBtn,
                null
            )

        } else if (v?.id == R.id.feed_menu2) {
            feedModule.menuButtonEvent(feed!!, supportFragmentManager)
        } else if(v?.id == R.id.image_btn){
            startActivity(FeedImageActivity::class.java) {
                putExtra("feed", feed)
            }
        }  else if (v?.id == R.id.comment_btn) {
            val timestamp = Timestamp(System.currentTimeMillis()).time

            if (answerMode) {
                if (modifyMode && answerComment != null) {
                    commentModule.submitCommentAnswer(
                        commentModule.findParentComment(answerPosition)!!, timestamp, modifyMode,
                        answerComment!!, answerPosition, commentPosition, binding!!.feedLayout.comment
                    )
                    initVariable()
                } else if (!modifyMode && curComment != null) {
                    commentModule.submitCommentAnswer(
                        curComment!!, timestamp, modifyMode, answerComment,
                        answerPosition, commentPosition, binding!!.feedLayout.comment
                    )

                    LocalRepository.getInstance(this)!!.getUserInfo(lifecycleScope, object :
                        LongTaskCallback<User> {
                        override fun onResponse(result: Result<User>) {
                            if(result is Result.Success) {

                                notificationModule.sendNotification(
                                    curComment!!.email,
                                    result.data.profileUri,
                                    binding!!.feedLayout.comment.text.toString(),
                                    timestamp,
                                    feed!!,
                                    lifecycleScope
                                )
                                initVariable()
                            }
                        }
                    })
                }
            } else {
                if (modifyMode) {
                    commentModule.submitComment(
                        curComment!!.timestamp, modifyMode, curComment, commentPosition,
                        binding!!.feedLayout.commentCount, binding!!.feedLayout.comment
                    )
                    initVariable()
                } else {
                    feed!!.setComment(feed!!.getComment() + 1)
                    commentModule.submitComment(
                        timestamp, modifyMode, curComment, commentPosition,
                        binding!!.feedLayout.commentCount, binding!!.feedLayout.comment
                    )

                    LocalRepository.getInstance(this)!!.getUserInfo(lifecycleScope, object :
                        LongTaskCallback<User> {
                        override fun onResponse(result: Result<User>) {
                            if(result is Result.Success) {
                                notificationModule.sendNotification(
                                    feed!!.getEmail()!!,
                                    result.data.profileUri,
                                    binding!!.feedLayout.comment.text.toString(),
                                    timestamp,
                                    feed!!,
                                    lifecycleScope
                                )
                                initVariable()
                            }
                        }
                    })
                }
            }
        } else if(v?.id == R.id.profile_btn) {
            startActivity(ShowFeedActivity::class.java) {
                putExtra("profileEmail", feed?.getEmail())
                putExtra("feedSort", "profile")
            }
        } else if(v?.id == R.id.share_btn){
            feed?.let { feedModule.sendShareLink(it, this, lifecycleScope) }
        }
    }


    fun initVariable() {
        answerMode = false
        curComment = null
        answerComment = null
        modifyMode = false
        CommonFunction.closeKeyboard(applicationContext, binding!!.feedLayout.comment)
        binding!!.feedLayout.comment.setText("")
        commentModule.setHint(binding!!.feedLayout.comment, binding!!.commentLayout.modeTitle, 0)
        binding!!.commentLayout.modeLayout.visibility = View.INVISIBLE
    }

    fun setIntent(){
        var intent = Intent(this, MainActivity::class.java)
        intent.putExtra("feed", feed)
        setResult(Constants.RESULT_OK, intent)
    }

    override fun onDismiss(dialog: DialogInterface?) {
        if (PreferenceManager.getString(applicationContext, "mode") == "comment_modify") {
            binding!!.commentLayout.modeLayout.visibility = View.VISIBLE
            modifyMode = true
            commentModule.setHint(binding!!.feedLayout.comment, binding!!.commentLayout.modeTitle, 3)
            binding!!.feedLayout.comment.requestFocus()
            CommonFunction.showKeyboard(this)

        } else if (PreferenceManager.getString(applicationContext, "mode") == "comment_answer_modify") {
            binding!!.commentLayout.modeLayout.visibility = View.VISIBLE
            modifyMode = true
            commentModule.setHint(binding!!.feedLayout.comment, binding!!.commentLayout.modeTitle, 2)
            binding!!.feedLayout.comment.requestFocus()
            CommonFunction.showKeyboard(this)

        } else if (PreferenceManager.getString(applicationContext, "mode") == "comment_delete") {
            commentModule.deleteComment(
                curComment, answerComment, commentPosition, answerPosition,
                answerMode, binding!!.feedLayout.commentCount
            )

        } else if(PreferenceManager.getString(applicationContext, "mode") == "feed_modify"){
            val intent = Intent(this, MakeFeedActivity::class.java).apply {
                putExtra("feed", feed)
                putExtra("mode", "modify")
            }

            startActivityForResult(intent, Constants.FEED_MODIFY_REQUEST)
        } else if(PreferenceManager.getString(applicationContext, "mode") == "feed_delete"){
            val intent = Intent(this, MakeFeedActivity::class.java).apply {
                putExtra("feed", feed)
                putExtra("mode", "delete")
            }

            startActivityForResult(intent, Constants.FEED_DELETE_REQUEST)
            finish()
        } else if(PreferenceManager.getString(applicationContext, "mode") == "follow") {
            feedModule.onDismiss(
                "follow", feed, this, LocalRepository.getInstance(
                    applicationContext
                )!!, null, lifecycleScope
            )

            CustomToast(applicationContext, getString(R.string.follow_toast)).show()
        } else if(PreferenceManager.getString(applicationContext, "mode") == "unfollow") {
            feedModule.onDismiss(
                "unfollow", feed, this, LocalRepository.getInstance(
                    applicationContext
                )!!, null, lifecycleScope
            )
            CustomToast(applicationContext, getString(R.string.unfollow_toast)).show()
        } else if(PreferenceManager.getString(applicationContext, "mode") == "report") {
            startActivity(ReportActivity::class.java) {
                putExtra("feed", feed)
            }
        }
    }

    override fun onBackPressed() {
        if (binding!!.commentLayout.modeLayout.visibility == View.VISIBLE) {
            initVariable()
        } else {
            setIntent()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == Constants.FEED_MODIFY_REQUEST && resultCode == Constants.RESULT_OK) {
            val intentFeed = data?.getParcelableExtra<Feed>("feed")

            if(intentFeed != null) {
                feedViewModel.setFeedLiveData(intentFeed)

                feed!!.setLocalUri(intentFeed.getLocalUri())
                feed!!.setRemoteUri(intentFeed.getRemoteUri())
                feed!!.setSort(intentFeed.getSort())
                feed!!.setHashTags(intentFeed.getHashTags())
                feed!!.setContent(intentFeed.getContent())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        binding = null
        commentAdapter = null
    }

    fun testToast(context: Context){
        CustomToast(context, "test").show()
    }
}