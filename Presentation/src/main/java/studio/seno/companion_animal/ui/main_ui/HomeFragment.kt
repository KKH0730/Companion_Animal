package studio.seno.companion_animal.ui.main_ui

import android.content.Intent
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
import androidx.recyclerview.widget.RecyclerView
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
import studio.seno.domain.model.User
import studio.seno.domain.util.PrefereceManager

/**
 * HomeFragment는 FeedViewListModel과 연결.
 * FeedViewModel는 FeedListAdapter와 연결.
 */
class HomeFragment : Fragment(), View.OnClickListener{
    private lateinit var binding: FragmentHomeBinding
    private val feedListViewModel: FeedListViewModel by viewModels()
    private val commentViewModel : CommentListViewModel by viewModels()
    private val mainViewModel : MainViewModel by viewModels()
    private var feedSort : String? = null
    private var feedPosition : Int? = null
    private var timeLineEmail : String? = null
    private val feedModule : FeedModule by lazy {
        FeedModule(feedListViewModel, commentViewModel, mainViewModel)
    }
    private var targetFeed : Feed? = null
    private val feedAdapter: FeedListAdapter by lazy { FeedListAdapter(parentFragmentManager, lifecycle, lifecycleScope) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            feedSort = it.getString("feedSort")
            feedPosition = it.getInt("feedPosition")
            timeLineEmail = it.getString("timeLineEmail")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(feedSort : String, feedPosition : Int, timeLineEmail : String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString("feedSort", feedSort)
                    putInt("feedPosition", feedPosition)
                    putString("timeLineEmail", timeLineEmail)
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

        binding.lifecycleOwner = viewLifecycleOwner
        binding.model = feedListViewModel
        feedAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.feedRecyclerView.adapter = feedAdapter

        init()

        feedItemEvent()
        if(feedSort == "feed_list") {
            binding.refreshLayout.isEnabled = true
            refreshFeedList()
        }
        loadFeedList()
        observe()
    }

    private fun init(){
        if(feedSort == "feed_list") {
            binding.header.findViewById<ImageButton>(R.id.back_btn).visibility = View.GONE
            binding.header.findViewById<TextView>(R.id.title).visibility = View.GONE
            binding.header.findViewById<ImageView>(R.id.logo).visibility = View.VISIBLE
            binding.header.findViewById<LinearLayout>(R.id.menu_set).visibility = View.VISIBLE
            binding.header.findViewById<ImageButton>(R.id.setting).visibility = View.GONE
        } else if(feedSort != null && feedSort == "feed_timeline")
            binding.header.visibility = View.GONE

        binding.refreshLayout.isEnabled = false
        binding.header.findViewById<ImageButton>(R.id.add).setOnClickListener(this)
        binding.header.findViewById<ImageButton>(R.id.search).setOnClickListener(this)
        binding.header.findViewById<ImageButton>(R.id.refresh).setOnClickListener(this)
    }

    private fun refreshFeedList(){
        binding.refreshLayout.setOnRefreshListener {
            loadFeedList()
        }
    }

    fun loadFeedList(){
        //게시판 데이터 서버로부터 불러와서 viewmode의 livedata 업데이트
        feedListViewModel.clearFeedList()
        if(feedSort != null && feedSort == "feed_list")
            feedListViewModel.requestLoadFeedList(null, "feed_list",
                null, binding.feedRecyclerView, object : LongTaskCallback<List<Feed>>{
                    override fun onResponse(result: Result<List<Feed>>) {
                        if (result is Result.Success) {
                            binding.refreshLayout.isRefreshing = false
                        } else if (result is Result.Error) {
                            Log.e("error", "feed refresh error : ${result.exception}")
                        }
                    }
                })

        else if(feedSort != null && feedSort == "feed_timeline") {
            feedListViewModel.requestLoadFeedList(null,
                "feed_timeline",
                timeLineEmail,
                binding.feedRecyclerView,
                object : LongTaskCallback<List<Feed>>{
                    override fun onResponse(result: Result<List<Feed>>) {
                        binding.feedRecyclerView.scrollToPosition(feedPosition!!)
                    }
                })
        }
    }

    private fun observe() {
        feedListViewModel.getFeedListLiveData().observe(requireActivity(), {
            feedAdapter.submitList(it)
        })
    }

    private fun feedItemEvent() {
        //댓글작성 버튼클릭
        feedAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onDetailClicked(feed : Feed, position : Int) {
                startActivityForResult(
                    intentFor<FeedDetailActivity>(
                        "feed" to feed,
                    ), Constants.FEED_DETAIL_REQUEST)
                PrefereceManager.setInt(requireContext(), "feed_position", position)

            }

            override fun onImageBtnClicked(feed: Feed) {
                startActivity<FeedImageActivity>("feed" to feed)
            }

            override fun onCommentBtnClicked(feed: Feed, commentEdit: EditText, commentCount: TextView, container: LinearLayout) {
                feedModule.onCommentBtnClicked(feed, commentEdit, commentCount, container)
            }

            override fun onCommentShowClicked(commentCountTextView: TextView, feed: Feed) {
                startActivityForResult(
                    intentFor<CommentActivity>(
                        "commentCount" to Integer.valueOf(commentCountTextView.text.toString()),
                        "feed" to feed,
                    ), Constants.COMMENT_REQUEST
                )
            }

            override fun onMenuClicked(feed: Feed, position: Int) {
                targetFeed = feed
                feedModule.menuButtonEvent(feed, parentFragmentManager)
            }

            override fun onHeartClicked(feed: Feed, heartCount: TextView, heartButton: ImageButton) {
                feedModule.heartButtonEvent(feed, heartCount, heartButton, feedAdapter)
            }

            override fun onBookmarkClicked(feed: Feed, bookmarkButton: ImageButton) {
                feedModule.bookmarkButtonEvent(feed, bookmarkButton, feedAdapter)
            }

            override fun onProfileLayoutClicked(feed: Feed) {
                startActivity<ShowFeedActivity>(
                    "profileEmail" to feed.email,
                    "feedSort" to "profile"
                )
            }


        })
    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.add) {
            startActivityForResult(
                intentFor<MakeFeedActivity>(), Constants.FEED_MAKE_QEQUEST
            )
        } else if(v?.id == R.id.search) {
            startActivity<SearchActivity>()
        } else if(v?.id == R.id.refresh) {
            loadFeedList()
        }
    }


    fun onDismissed(type: String) {
        if(targetFeed != null) {
            if(type == "feed_modify") {
                feedModule.onDismiss(
                    "feed_modify", targetFeed, requireActivity(), LocalRepository.getInstance(requireContext())!!, feedAdapter, lifecycleScope)

            } else if(type == "feed_delete") {
                feedModule.onDismiss("feed_delete", targetFeed, requireActivity(), LocalRepository.getInstance(requireContext())!!, feedAdapter, lifecycleScope)

            } else if(type == "follow") {
                feedModule.onDismiss("follow", targetFeed, requireActivity(), LocalRepository.getInstance(requireContext())!!, feedAdapter, lifecycleScope)

            } else if(type == "unfollow") {
                feedModule.onDismiss("unfollow", targetFeed, requireActivity(), LocalRepository.getInstance(requireContext())!!, feedAdapter, lifecycleScope)
            }
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == Constants.COMMENT_REQUEST && resultCode == Constants.RESULT_OK){

        } else if(requestCode == Constants.FEED_DETAIL_REQUEST && resultCode == Constants.RESULT_OK) {

        } else if(requestCode == Constants.FEED_MAKE_QEQUEST && resultCode == Constants.RESULT_OK) {
            loadFeedList()
        }
    }
}

