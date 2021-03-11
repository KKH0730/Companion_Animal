package studio.seno.companion_animal.ui.comment

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import org.jetbrains.anko.startActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import studio.seno.companion_animal.MainActivity
import studio.seno.companion_animal.R
import studio.seno.companion_animal.ui.ReportActivity
import studio.seno.companion_animal.databinding.ActivityCommentBinding
import studio.seno.companion_animal.module.CommentModule
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.module.NotificationModule
import studio.seno.companion_animal.ui.MenuDialog
import studio.seno.companion_animal.util.Constants
import studio.seno.datamodule.repository.local.LocalRepository
import studio.seno.domain.model.Comment
import studio.seno.domain.model.Feed
import studio.seno.domain.model.User
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.PreferenceManager
import studio.seno.domain.util.Result
import java.sql.Timestamp

const val SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD = 128

class CommentActivity : AppCompatActivity(), View.OnClickListener,
    DialogInterface.OnDismissListener {
    private var binding: ActivityCommentBinding? = null
    private val commentListViewModel: CommentListViewModel by viewModel()
    private var commentAdapter : CommentAdapter? = CommentAdapter()
    private var answerMode = false
    private var modifyMode = false
    private var keybord = false
    private lateinit var commentCountText: TextView
    private var profileUri : String? = null
    private var curComment: Comment? = null
    private var answerComment: Comment? = null
    private var answerPosition = 0
    private var commentPosition = 0
    private val feed: Feed by lazy { intent.getParcelableExtra<Feed>("feed") }
    private lateinit var commentModule : CommentModule
    private lateinit var notificationModule : NotificationModule


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_comment)
        binding!!.lifecycleOwner = this
        binding!!.model = commentListViewModel

        initView()
        LocalRepository.getInstance(this)!!.getUserInfo(lifecycleScope, object :
            LongTaskCallback<User> {
            override fun onResponse(result: Result<User>) {
                if(result is Result.Success) {
                    profileUri = result.data.profileUri
                    Glide.with(this@CommentActivity)
                        .load(profileUri)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(binding!!.profileImageVIew)

                    notificationModule = NotificationModule(applicationContext, result.data.nickname)
                    commentModule = CommentModule(
                        commentListViewModel, feed, result.data.email, result.data.nickname,
                        applicationContext, commentAdapter!!
                    )

                    checkKeyboardStatus()
                    commentEvent()
                    observe()
                }
            }
        })
    }

    private fun initView() {

        binding!!.lifecycleOwner = this
        binding!!.model = commentListViewModel
        binding!!.commentRecyclerView.adapter = commentAdapter

        commentCountText = binding!!.header.findViewById(R.id.comment_count)
        binding!!.header.findViewById<TextView>(R.id.comment_count).apply {
            visibility = View.VISIBLE
            text = intent.getIntExtra("commentCount", 0).toString()
        }

        if (intent.getParcelableExtra<Feed>("feed") != null)
            commentListViewModel.requestLoadComment(feed.getEmail()!!, feed.getTimestamp())

        binding!!.header.findViewById<TextView>(R.id.title2).text = getString(R.string.header_title)
        binding!!.header.findViewById<ImageButton>(R.id.back_btn).setOnClickListener(this)
        binding!!.commentBtn.setOnClickListener(this)
        binding!!.showCommentBar.setOnClickListener(this)
        binding!!.comment.addTextChangedListener(textWatcher)
    }

    fun commentEvent() {
        commentAdapter!!.setOnEventListener(object : OnCommentEventListener {
            override fun onReadAnswerClicked(readAnswer: Button, targetComment: Comment) {
                if (targetComment.getChildren()!!.isNotEmpty())
                    commentModule.showComment(readAnswer, targetComment)
                else {
                    commentModule.hideComment(readAnswer, targetComment)
                }
            }

            override fun onWriteAnswerClicked(targetComment: Comment, position: Int) {
                commentModule.setHint(binding!!.comment, binding!!.modeTitle, 1)
                curComment = targetComment
                commentPosition = position
                answerMode = true

                binding!!.modeLayout.visibility = View.VISIBLE
                showCommentContainer()

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


    private fun observe() {
        commentListViewModel.getCommentListLiveData().observe(this, {
            commentAdapter!!.submitList(it)
        })
    }

    fun setIntent(){
        var intent = Intent(this, MainActivity::class.java)
        intent.putExtra("comment_count", commentCountText.text.toString())
        intent.putExtra("feed", feed)
        setResult(Constants.RESULT_OK, intent)
    }

    private val textWatcher : TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if(binding!!.comment.text.isEmpty())
                binding!!.commentBtn.visibility = View.INVISIBLE
            else
                binding!!.commentBtn.visibility = View.VISIBLE
        }

        override fun afterTextChanged(s: Editable?) {}
    }


    override fun onClick(v: View?) {
        if (v?.id == R.id.back_btn) {
            setIntent()
            finish()
        }  else if (v?.id == R.id.comment_btn) {
            val timestamp = Timestamp(System.currentTimeMillis()).time

            if (answerMode) { // 답글쓰기 모드
                if (modifyMode && answerComment != null) { //답글 수정 모드
                        commentModule.submitCommentAnswer(
                            commentModule.findParentComment(answerPosition)!!, answerComment!!.timestamp, modifyMode, answerComment!!, answerPosition,
                            commentPosition, binding!!.comment
                        )
                } else if (!modifyMode && curComment != null) { //일반 답글 모드
                    commentModule.submitCommentAnswer(
                        curComment!!, timestamp, modifyMode, answerComment,
                        answerPosition, commentPosition, binding!!.comment
                    )

                    notificationModule.sendNotification(curComment!!.email, profileUri, binding!!.comment.text.toString(), timestamp, feed, lifecycleScope)
                }
            } else {
                if (modifyMode) { //댓글 수정 모드
                    commentModule.submitComment(
                        curComment!!.timestamp, modifyMode, curComment, commentPosition,
                        binding!!.header.findViewById(R.id.comment_count), binding!!.comment
                    )
                } else { // 일반 댓글 모드
                    commentModule.submitComment(
                        timestamp, modifyMode, curComment, commentPosition,
                        binding!!.header.findViewById(R.id.comment_count), binding!!.comment
                    )

                    notificationModule.sendNotification(feed.getEmail()!!, profileUri, binding!!.comment.text.toString(), timestamp, feed, lifecycleScope)
                }
            }
            initVariable()

        } else if(v?.id == R.id.show_comment_bar) {
            showCommentContainer()
        }
    }

    private fun initVariable() {
        answerMode = false
        curComment = null
        answerComment = null
        modifyMode = false
        CommonFunction.closeKeyboard(applicationContext, binding!!.comment)
        binding!!.comment.setText("")
        commentModule.setHint(binding!!.comment, binding!!.modeTitle, 0)
        binding!!.modeLayout.visibility = View.INVISIBLE
    }

    private fun showCommentContainer(){
        binding!!.commentContainer.visibility = View.VISIBLE
        CommonFunction.getInstance()!!.showKeyboard(this)
        binding!!.comment.requestFocus()
    }

    private fun checkKeyboardStatus(){
        binding!!.rootView.viewTreeObserver.addOnGlobalLayoutListener {
            keybord = isKeyboardShown(binding!!.rootView.rootView)
        }
    }

    private fun isKeyboardShown(rootView : View) : Boolean {
        val r = Rect()
        rootView.getWindowVisibleDisplayFrame(r)
        val dm = rootView.resources.displayMetrics
        val heightDiff = rootView.bottom - r.bottom

        return heightDiff > SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD * dm.density
    }

    override fun onBackPressed() {
        if (binding!!.modeLayout.visibility == View.VISIBLE || keybord) {
            initVariable()
        } else {
            if(binding!!.commentContainer.visibility == View.VISIBLE)
                binding!!.commentContainer.visibility = View.GONE
            else {
                setIntent()
                finish()
            }
        }
    }


    override fun onDismiss(dialog: DialogInterface?) {
        if (PreferenceManager.getString(applicationContext, "mode") == "comment_modify") {
            binding!!.modeLayout.visibility = View.VISIBLE
            modifyMode = true
            commentModule.setHint(binding!!.comment, binding!!.modeTitle, 3)
            CommonFunction.showKeyboard(this)
        } else if (PreferenceManager.getString(applicationContext, "mode") == "comment_answer_modify"
        ) {
            binding!!.modeLayout.visibility = View.VISIBLE
            modifyMode = true
            commentModule.setHint(binding!!.comment, binding!!.modeTitle, 2)
            CommonFunction.showKeyboard(this)
        } else if (PreferenceManager.getString(applicationContext, "mode") == "comment_delete") {
            commentModule.deleteComment(
                curComment, answerComment, commentPosition, answerPosition,
                answerMode, binding!!.header.findViewById(R.id.comment_count)
            )
        }  else if(PreferenceManager.getString(applicationContext, "mode") == "report") {
            startActivity<ReportActivity>("feed" to feed)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        binding = null
        commentAdapter = null
    }
}