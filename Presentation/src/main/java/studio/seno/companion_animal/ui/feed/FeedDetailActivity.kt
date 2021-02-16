package studio.seno.companion_animal.ui.feed


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.ActivityFeedDetailBinding
import studio.seno.companion_animal.module.FeedModule
import studio.seno.companion_animal.ui.comment.CommentAdapter
import studio.seno.companion_animal.ui.comment.CommentListViewModel
import studio.seno.companion_animal.ui.main_ui.MainViewModel
import studio.seno.domain.model.Comment
import studio.seno.domain.model.Feed

class FeedDetailActivity : AppCompatActivity(), View.OnClickListener {
    private var feed : Feed? = null
    private lateinit var binding : ActivityFeedDetailBinding
    private lateinit var feedViewModel: FeedViewModel
    private val feedListViewModel: FeedListViewModel by viewModels()
    private val commentViewModel : CommentListViewModel by viewModels()
    private val mainViewModel : MainViewModel by viewModels()
    private val commentListViewModel: CommentListViewModel by viewModels()
    private val feedModule : FeedModule by lazy{ FeedModule(feedListViewModel, commentViewModel, mainViewModel) }
    private var curComment: Comment? = null
    private var curCommentPosition = 0
    private var answerComment : Comment? = null
    private var answerPosition = 0
    private val commentAdapter = CommentAdapter()
    private var answerMode = false
    private var modifyMode = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_feed_detail)

        init()

        binding.feedLayout.lifecycleOwner = this
        binding.feedLayout.model = feedViewModel
        binding.feedLayout.executePendingBindings()

        binding.commentLayout.lifecycleOwner = this
        binding.commentLayout.model = commentListViewModel





    }

    fun init(){
        feedViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory{
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return FeedViewModel(lifecycle, supportFragmentManager, binding.feedLayout.indicator) as T
            }
        }).get(FeedViewModel::class.java)

        if(intent.getParcelableExtra<Feed>("feed") == null)
            finish()
        else {
            feed = intent.getParcelableExtra<Feed>("feed")
            feedViewModel.setFeedLiveData(feed!!)
            binding.feedLayout.commentShow.visibility = View.GONE
            binding.feedLayout.commentCount.visibility = View.GONE
            commentListViewModel.requestLoadComment(feed!!.email, feed!!.timestamp)
        }


        binding.commentLayout.header.visibility = View.GONE
        binding.commentLayout.modeCloseBtn.setOnClickListener(this)
        binding.feedLayout.bookmarkBtn.setOnClickListener(this)
        binding.feedLayout.heartBtn.setOnClickListener(this)
        binding.feedLayout.feedMenu.setOnClickListener(this)
        binding.feedLayout.commentBtn.setOnClickListener(this)
        binding.feedLayout.comment.addTextChangedListener(textWatcher)

        binding.commentLayout.commentRecyclerView.adapter = commentAdapter
    }




    private val textWatcher : TextWatcher = object: TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if(binding.feedLayout.comment.text.isEmpty())
                binding.feedLayout.commentBtn.visibility = View.INVISIBLE
            else
                binding.feedLayout.commentBtn.visibility = View.VISIBLE
        }
    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.bookmark_btn) {
            feedModule.bookmarkButtonEvent(feed!!, binding.feedLayout.bookmarkBtn, null)
        } else if(v?.id == R.id.heart_btn) {
            feedModule.heartButtonEvent(feed!!, binding.feedLayout.heartCount, binding.feedLayout.heartBtn, null)
        } else if(v?.id == R.id.feed_menu) {
            feedModule.menuButtonEvent(feed!!, supportFragmentManager)
        } else if(v?.id == R.id.mode_close_btn) {

        } else if(v?.id == R.id.comment_btn) {

        }
    }


}