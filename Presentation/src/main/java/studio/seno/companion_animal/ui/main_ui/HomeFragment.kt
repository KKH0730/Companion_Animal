package studio.seno.companion_animal.ui.main_ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.startActivity
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentHomeBinding
import studio.seno.companion_animal.module.FeedModule
import studio.seno.companion_animal.ui.comment.CommentActivity
import studio.seno.companion_animal.ui.comment.CommentListViewModel
import studio.seno.companion_animal.ui.feed.*
import studio.seno.companion_animal.ui.search.SearchActivity
import studio.seno.companion_animal.util.Constants
import studio.seno.datamodule.LocalRepository
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Feed

/**
 * HomeFragment는 FeedViewListModel과 연결.
 * FeedViewModel는 FeedListAdapter와 연결.
 */
class HomeFragment : Fragment(), View.OnClickListener{
    private lateinit var binding: FragmentHomeBinding
    private val feedListViewModel: FeedListViewModel by viewModels()
    private val commentViewModel : CommentListViewModel by viewModels()
    private val mainViewModel : MainViewModel by viewModels()
    private val localRepository : LocalRepository by lazy {
        LocalRepository(requireContext())
    }
    private val feedModule : FeedModule by lazy {
        FeedModule(feedListViewModel, commentViewModel, mainViewModel)
    }
    private var targetFeed : Feed? = null
    private var targetFeedPosition = 0
    private val currentUserEmail  = FirebaseAuth.getInstance().currentUser?.email.toString()
    private val feedAdapter: FeedListAdapter by lazy { FeedListAdapter(parentFragmentManager, lifecycle, lifecycleScope) }

    companion object {
        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_home, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = requireActivity()
        binding.model = feedListViewModel
        binding.feedRecyclerView.adapter = feedAdapter

        init()

        feedItemEvent()
        refreshFeedList()


        //게시판 데이터 서버로부터 불러와서 viewmode의 livedata 업데이트
        feedListViewModel.clearFeedList()
        feedListViewModel.requestLoadFeedList(null, binding.feedRecyclerView, null)
        observe()
    }

    private fun init(){
        binding.header.findViewById<ImageButton>(R.id.back_btn).visibility = View.GONE
        binding.header.findViewById<TextView>(R.id.title).visibility = View.GONE
        binding.header.findViewById<ImageView>(R.id.logo).visibility = View.VISIBLE
        binding.header.findViewById<LinearLayout>(R.id.menu_set).visibility = View.VISIBLE
        binding.header.findViewById<ImageButton>(R.id.setting).visibility = View.GONE
        binding.header.findViewById<ImageButton>(R.id.add).setOnClickListener(this)
        binding.header.findViewById<ImageButton>(R.id.search).setOnClickListener(this)
        binding.header.findViewById<ImageButton>(R.id.refresh).setOnClickListener(this)
    }

    private fun observe() {
        feedListViewModel.getFeedListLiveData().observe(requireActivity(), {
            feedAdapter.submitList(it)
        })

    }

    private fun feedItemEvent() {
        //댓글작성 버튼클릭
        feedAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onDetailClicked(feed: Feed) {
                startActivity<FeedDetailActivity>("feed" to feed)
            }

            override fun onImageBtnClicked(feed: Feed) {
                startActivity<FeedImageActivity>("feed" to feed)
            }

            override fun onCommentBtnClicked(feed: Feed, commentEdit: EditText, commentCount: TextView, container: LinearLayout) {
                feedModule.onCommentBtnClicked(feed, commentEdit, commentCount, container)
            }

            override fun onCommentShowClicked(commentCount: TextView, feed: Feed) {
                startActivityForResult(
                    intentFor<CommentActivity>(
                        "commentCount" to Integer.valueOf(commentCount.text.toString()),
                        "feed" to feed,
                    ), Constants.COMMENT_REQUEST
                )
            }

            override fun onMenuClicked(feed: Feed, position: Int) {
                targetFeed = feed
                targetFeedPosition = position
                feedModule.menuButtonEvent(feed, parentFragmentManager)
            }

            override fun onHeartClicked(feed: Feed, heartCount: TextView, heartButton: ImageButton) {
                feedModule.heartButtonEvent(feed, heartCount, heartButton, feedAdapter)
            }

            override fun onBookmarkClicked(feed: Feed, bookmarkButton: ImageButton) {
                feedModule.bookmarkButtonEvent(feed, bookmarkButton, feedAdapter)
            }


        })
    }


    private fun refreshFeedList(){
        binding.refreshLayout.setOnRefreshListener {
            feedListViewModel.clearFeedList()

            feedListViewModel.requestLoadFeedList(null, binding.feedRecyclerView, object : LongTaskCallback<List<Feed>> {
                override fun onResponse(result: Result<List<Feed>>) {
                    if (result is Result.Success) {
                        binding.refreshLayout.isRefreshing = false
                    } else if (result is Result.Error) {
                        Log.e("error", "feed refresh error : ${result.exception}")
                    }
                }
            })
        }
    }


    fun onDismissed(type: String) {
        if(targetFeed != null) {
            if(type == "feed_modify") {

                startActivity<MakeFeedActivity>(
                    "feed" to targetFeed,
                    "mode" to "modify",
                )

            } else if(type == "feed_delete") {
                feedListViewModel.setFeedListLiveData(feedAdapter.currentList.toMutableList())
                startActivity<MakeFeedActivity>(
                    "feed" to targetFeed,
                    "mode" to "delete"
                )
            } else if(type == "follow") {
                if(targetFeed != null) {
                    feedListViewModel.requestUpdateFollower(targetFeed!!, currentUserEmail, true)
                    localRepository.updateFollowing(lifecycleScope, true)
                }
            } else if(type == "unfollow") {
                if(targetFeed != null) {
                    feedListViewModel.requestUpdateFollower(targetFeed!!, currentUserEmail, false)
                    localRepository.updateFollowing(lifecycleScope,false)
                }
            }
        }
    }



    override fun onClick(v: View?) {
        if(v?.id == R.id.add) {
            startActivity<MakeFeedActivity>()
        } else if(v?.id == R.id.search) {
            startActivity<SearchActivity>()
        } else if(v?.id == R.id.refresh) {
            feedListViewModel.clearFeedList()
            feedListViewModel.requestLoadFeedList(null, binding.feedRecyclerView, null)
        }
    }
}

