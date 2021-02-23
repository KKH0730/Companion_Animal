package studio.seno.companion_animal.ui.feed


import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.startActivity
import studio.seno.companion_animal.MainActivity
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.ActivityFeedDetailBinding
import studio.seno.companion_animal.module.CommentModule
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.module.FeedModule
import studio.seno.companion_animal.module.NotificationModule
import studio.seno.companion_animal.ui.MenuDialog
import studio.seno.companion_animal.ui.comment.CommentAdapter
import studio.seno.companion_animal.ui.comment.CommentListViewModel
import studio.seno.companion_animal.ui.comment.OnCommentEventListener
import studio.seno.companion_animal.ui.main_ui.MainViewModel
import studio.seno.companion_animal.util.Constants
import studio.seno.datamodule.LocalRepository
import studio.seno.domain.model.Comment
import studio.seno.domain.model.Feed
import studio.seno.domain.util.PrefereceManager
import java.sql.Timestamp

class FeedDetailActivity : AppCompatActivity(), View.OnClickListener,
    DialogInterface.OnDismissListener {
    private var feed: Feed? = null
    private lateinit var binding: ActivityFeedDetailBinding
    private lateinit var feedViewModel: FeedViewModel
    private val feedListViewModel: FeedListViewModel by viewModels()
    private val commentViewModel: CommentListViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private val commentListViewModel: CommentListViewModel by viewModels()
    private val feedModule: FeedModule by lazy {
        FeedModule(
            feedListViewModel,
            commentViewModel,
            mainViewModel
        )
    }
    private var curComment: Comment? = null
    private var answerComment: Comment? = null
    private var answerPosition = 0
    private var commentPosition = 0
    private val commentAdapter = CommentAdapter()
    private var answerMode = false
    private var modifyMode = false
    private var backKeyPressedTime = 0L
    private val commentModule: CommentModule by lazy {
        CommentModule(
            mainViewModel, commentListViewModel, feed!!,
            FirebaseAuth.getInstance().currentUser?.email.toString(),
            PrefereceManager.getString(applicationContext, "nickName")!!,
            applicationContext, commentAdapter
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_feed_detail)

        init()


        binding.feedLayout.lifecycleOwner = this
        binding.feedLayout.model = feedViewModel
        binding.feedLayout.executePendingBindings()

        binding.commentLayout.lifecycleOwner = this
        binding.commentLayout.model = commentListViewModel

        commentItemEvent()
        observe()

    }

    fun init() {
        feedViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return FeedViewModel(
                    lifecycle,
                    supportFragmentManager,
                    binding.feedLayout.indicator,
                    lifecycleScope
                ) as T
            }
        }).get(FeedViewModel::class.java)

        if (intent.getParcelableExtra<Feed>("feed") == null)
            finish()
        else {
            feed = intent.getParcelableExtra("feed")
            feedViewModel.setFeedLiveData(feed!!)
            binding.feedLayout.commentShow.visibility = View.GONE
            commentListViewModel.requestLoadComment(feed!!.email, feed!!.timestamp)
        }


        binding.header.findViewById<TextView>(R.id.title).visibility = View.GONE
        binding.header.findViewById<ImageButton>(R.id.back_btn).setOnClickListener(this)
        binding.commentLayout.commentContainer.visibility = View.GONE
        binding.commentLayout.header.visibility = View.GONE
        binding.commentLayout.modeCloseBtn.setOnClickListener(this)
        binding.feedLayout.detailBtn.visibility = View.GONE
        binding.feedLayout.bookmarkBtn.setOnClickListener(this)
        binding.feedLayout.heartBtn.setOnClickListener(this)
        binding.feedLayout.feedMenu.setOnClickListener(this)
        binding.feedLayout.commentBtn.setOnClickListener(this)
        binding.feedLayout.imageBtn.setOnClickListener(this)
        binding.feedLayout.profileLayout.setOnClickListener(this)
        binding.feedLayout.comment.addTextChangedListener(textWatcher)

        binding.commentLayout.commentRecyclerView.adapter = commentAdapter
    }

    fun commentItemEvent() {
        commentAdapter.setOnEventListener(object : OnCommentEventListener {
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

                commentModule.setHint(
                    binding.feedLayout.comment,
                    binding.commentLayout.modeTitle,
                    1
                )
                answerMode = true
                binding.commentLayout.modeLayout.visibility = View.VISIBLE
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
            if (binding.feedLayout.comment.text.isEmpty())
                binding.feedLayout.commentBtn.visibility = View.INVISIBLE
            else
                binding.feedLayout.commentBtn.visibility = View.VISIBLE
        }
    }

    private fun observe() {
        commentListViewModel.getCommentListLiveData().observe(this, {
            commentAdapter.submitList(it)
        })
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.back_btn) {
            finish()
        } else if (v?.id == R.id.bookmark_btn) {
            feedModule.bookmarkButtonEvent(feed!!, binding.feedLayout.bookmarkBtn, null)

        } else if (v?.id == R.id.heart_btn) {
            feedModule.heartButtonEvent(
                feed!!,
                binding.feedLayout.heartCount,
                binding.feedLayout.heartBtn,
                null
            )
        } else if (v?.id == R.id.feed_menu) {
            feedModule.menuButtonEvent(feed!!, supportFragmentManager)
        } else if(v?.id == R.id.image_btn){
            startActivity<FeedImageActivity>("feed" to feed)
        } else if (v?.id == R.id.mode_close_btn) {
            initVariable()
        } else if (v?.id == R.id.comment_btn) {
            val timestamp = Timestamp(System.currentTimeMillis()).time
            val notificationModule = NotificationModule(applicationContext, mainViewModel)

            if (answerMode) {
                if (modifyMode && answerComment != null) {
                    commentModule.submitCommentAnswer(
                        commentModule.findParentComment(answerPosition)!!, timestamp, modifyMode,
                        answerComment!!, answerPosition, commentPosition, binding.feedLayout.comment
                    )
                } else if (!modifyMode && curComment != null) {
                    commentModule.submitCommentAnswer(
                        curComment!!, timestamp, modifyMode, answerComment,
                        answerPosition, commentPosition, binding.feedLayout.comment
                    )

                    notificationModule.sendNotification(curComment!!.email, binding.feedLayout.comment.text.toString(), timestamp, feed!!)
                }
            } else {
                if (modifyMode) {
                    commentModule.submitComment(
                        curComment!!.timestamp, modifyMode, curComment, commentPosition,
                        binding.feedLayout.commentCount, binding.feedLayout.comment
                    )
                } else {
                    commentModule.submitComment(
                        timestamp, modifyMode, curComment, commentPosition,
                        binding.feedLayout.commentCount, binding.feedLayout.comment
                    )

                    notificationModule.sendNotification(feed!!.email, binding.feedLayout.comment.text.toString(), timestamp, feed!!)
                }
            }
            initVariable()
        } else if(v?.id == R.id.profile_layout) {
            startActivity<ShowFeedActivity>(
                "profileEmail" to feed?.email,
                "feedSort" to "profile"
            )
        }
    }


    fun initVariable() {
        answerMode = false
        curComment = null
        answerComment = null
        modifyMode = false
        CommonFunction.closeKeyboard(applicationContext, binding.feedLayout.comment)
        binding.feedLayout.comment.setText("")
        commentModule.setHint(binding.feedLayout.comment, binding.commentLayout.modeTitle, 0)
        binding.commentLayout.modeLayout.visibility = View.INVISIBLE
    }

    override fun onDismiss(dialog: DialogInterface?) {
        if (PrefereceManager.getString(applicationContext, "mode") == "comment_modify") {
            binding.commentLayout.modeLayout.visibility = View.VISIBLE
            modifyMode = true
            commentModule.setHint(binding.feedLayout.comment, binding.commentLayout.modeTitle, 3)
            CommonFunction.showKeyboard(this)

        } else if (PrefereceManager.getString(applicationContext, "mode") == "comment_answer_modify") {
            binding.commentLayout.modeLayout.visibility = View.VISIBLE
            modifyMode = true
            commentModule.setHint(binding.feedLayout.comment, binding.commentLayout.modeTitle, 2)
            CommonFunction.showKeyboard(this)

        } else if (PrefereceManager.getString(applicationContext, "mode") == "comment_delete") {
            commentModule.deleteComment(
                curComment, answerComment, commentPosition, answerPosition,
                answerMode, binding.feedLayout.commentCount
            )

        } else if(PrefereceManager.getString(applicationContext, "mode") == "feed_modify"){
            feedModule.onDismiss(
                "feed_modify", feed, this, LocalRepository.getInstance(applicationContext)!!, null, lifecycleScope)
        } else if(PrefereceManager.getString(applicationContext, "mode") == "feed_delete"){
            feedModule.onDismiss("feed_delete", feed, this, LocalRepository.getInstance(applicationContext)!!, null, lifecycleScope)
            finish()
        } else if(PrefereceManager.getString(applicationContext, "mode") == "follow") {
            feedModule.onDismiss("follow", feed, this, LocalRepository.getInstance(applicationContext)!!, null, lifecycleScope)
        } else if(PrefereceManager.getString(applicationContext, "mode") == "unfollow") {
            feedModule.onDismiss("unfollow", feed, this, LocalRepository.getInstance(applicationContext)!!, null, lifecycleScope)
        }
    }

    override fun onBackPressed() {
        if (binding.commentLayout.modeLayout.visibility == View.VISIBLE) {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis()
                initVariable()
            } else {
                finish()
            }
        } else
            finish()
    }

    override fun onDestroy() {
        super.onDestroy()

        var intent = Intent(this, MainActivity::class.java)
        intent.putExtra("feed", feed)
        setResult(Constants.RESULT_OK, intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == Constants.FEED_MODIFY_REQUEST && resultCode == Constants.RESULT_OK) {
            Log.d("hi","intent : ${data?.getParcelableExtra<Feed>("feed")?.remoteUri?.size}")
        }
    }
}