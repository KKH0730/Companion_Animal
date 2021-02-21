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
import androidx.recyclerview.widget.LinearLayoutManager
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
import studio.seno.domain.database.AppDatabase
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
    private var sort : String? = null
    private var position : Int? = null
    private val localRepository : LocalRepository by lazy {
        LocalRepository(requireContext())
    }
    private val feedModule : FeedModule by lazy {
        FeedModule(feedListViewModel, commentViewModel, mainViewModel)
    }
    private var targetFeed : Feed? = null
    private var targetFeedPosition = 0
    private val feedAdapter: FeedListAdapter by lazy { FeedListAdapter(parentFragmentManager, lifecycle, lifecycleScope) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sort = it.getString("sort")
            position = it.getInt("position")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(sort : String, position : Int) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString("sort", sort)
                    putInt("position", position)
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
        refreshFeedList()
        loadFeedList()
        observe()
    }

    private fun init(){
        if(sort == "whole_feed_list") {
            binding.header.findViewById<ImageButton>(R.id.back_btn).visibility = View.GONE
            binding.header.findViewById<TextView>(R.id.title).visibility = View.GONE
            binding.header.findViewById<ImageView>(R.id.logo).visibility = View.VISIBLE
            binding.header.findViewById<LinearLayout>(R.id.menu_set).visibility = View.VISIBLE
            binding.header.findViewById<ImageButton>(R.id.setting).visibility = View.GONE
        } else if(sort != null && sort == "my_feed_list") {
            binding.header.visibility = View.GONE
        }

        binding.header.findViewById<ImageButton>(R.id.add).setOnClickListener(this)
        binding.header.findViewById<ImageButton>(R.id.search).setOnClickListener(this)
        binding.header.findViewById<ImageButton>(R.id.refresh).setOnClickListener(this)
    }

    private fun observe() {
        feedListViewModel.getFeedListLiveData().observe(requireActivity(), {
            feedAdapter.submitList(it)
        })

    }

    fun loadFeedList(){
        //게시판 데이터 서버로부터 불러와서 viewmode의 livedata 업데이트
        feedListViewModel.clearFeedList()
        if(sort != null && sort == "whole_feed_list")
            feedListViewModel.requestLoadFeedList(null, "feedList", null, binding.feedRecyclerView, null)
        else if(sort != null && sort == "my_feed_list") {
            feedListViewModel.requestLoadFeedList(null,
                "myFeed",
                FirebaseAuth.getInstance().currentUser?.email.toString(),
                binding.feedRecyclerView,
                object : LongTaskCallback<List<Feed>>{
                    override fun onResponse(result: Result<List<Feed>>) {
                        binding.feedRecyclerView.scrollToPosition(position!!)
                    }
                })
        }
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
            feedListViewModel.requestLoadFeedList(null, "feedList", null, binding.feedRecyclerView, object : LongTaskCallback<List<Feed>> {
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
                localRepository.getUserInfo(lifecycleScope, object : LongTaskCallback<User>{
                    override fun onResponse(result: Result<User>) {
                        if(result is Result.Success) {
                            feedListViewModel.requestUpdateFollower(targetFeed!!.email,  targetFeed!!.nickname, targetFeed!!.remoteProfileUri, true, result.data.nickname, result.data.profileUri)
                            localRepository.updateFollowing(lifecycleScope, true)

                        } else if(result is Result.Error) {
                            Log.e("error", "Homefragment follow error : ${result.exception}")
                        }
                    }
                })
            } else if(type == "unfollow") {
                localRepository.getUserInfo(lifecycleScope, object : LongTaskCallback<User>{
                    override fun onResponse(result: Result<User>) {
                        if(result is Result.Success) {
                            feedListViewModel.requestUpdateFollower(targetFeed!!.email,  targetFeed!!.nickname, targetFeed!!.remoteProfileUri, false, result.data.nickname, result.data.profileUri)
                            localRepository.updateFollowing(lifecycleScope,false)

                        } else if(result is Result.Error) {
                            Log.e("error", "Homefragment follow error : ${result.exception}")
                        }
                    }
                })
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
            feedListViewModel.requestLoadFeedList(null, "feedList", null, binding.feedRecyclerView, null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == Constants.COMMENT_REQUEST && resultCode == Constants.RESULT_OK){

        } else if(requestCode == Constants.FEED_DETAIL_REQUEST && resultCode == Constants.RESULT_OK) {

        }
    }
}

