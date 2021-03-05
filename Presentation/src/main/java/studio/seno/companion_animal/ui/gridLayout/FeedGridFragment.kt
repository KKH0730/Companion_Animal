package studio.seno.companion_animal.ui.gridLayout

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.support.v4.startActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentFeedGridBinding
import studio.seno.companion_animal.ui.feed.FeedDetailActivity
import studio.seno.companion_animal.ui.feed.FeedListViewModel
import studio.seno.companion_animal.ui.feed.ShowFeedActivity
import studio.seno.companion_animal.ui.search.OnSearchItemClickListener
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result
import studio.seno.domain.model.Feed

class FeedGridFragment : Fragment() {
    private val feedListViewModel: FeedListViewModel by viewModel()
    private lateinit var binding : FragmentFeedGridBinding
    private val gridImageAdapter = GridImageAdapter()
    private var keyword : String? = null
    private var feedSort : String? = null
    private var timeLineEmail : String? = null


    companion object {
        @JvmStatic
        fun newInstance(keyword : String?, feedSort : String, timeLineEmail : String?) =
            FeedGridFragment().apply {
                arguments = Bundle().apply {
                    putString("keyword", keyword)
                    putString("feedSort", feedSort)
                    putString("timeLineEmail", timeLineEmail)
                }
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            keyword = it.getString("keyword")
            feedSort = it.getString("feedSort")
            timeLineEmail = it.getString("timeLineEmail")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_feed_grid, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.feedListViewModel = feedListViewModel

        init()

        setFeedList()
        feedItemEvent()
        observe()
    }

    private fun init(){
        binding.gridRecyclerview.adapter = gridImageAdapter
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun setFeedList() {
        feedListViewModel.clearFeedList()


        if(feedSort == "feed_timeline") {
            feedListViewModel.getPagingFeed(
                null, null, null,
                null,
                "feed_timeline",
                timeLineEmail,
                binding.gridRecyclerview,
                object : LongTaskCallback<List<Feed>> {
                    override fun onResponse(result: Result<List<Feed>>) {
                        binding.progressBar.visibility = View.GONE
                        binding.gridRecyclerview.visibility = View.VISIBLE
                    }
                }
            )
        } else if(feedSort == "feed_search"){
            feedListViewModel.getPagingFeed(
                null, null, null,
                keyword,
                "feed_search",
                null,
                binding.gridRecyclerview,
                object : LongTaskCallback<List<Feed>> {
                override fun onResponse(result: Result<List<Feed>>) {
                    binding.progressBar.visibility = View.GONE
                    binding.gridRecyclerview.visibility = View.VISIBLE
                    if(result is Result.Success) {

                        if(result.data == null) {
                            binding.noResultTextView.setText("\"" + keyword + "\"" + getString(R.string.no_result))
                            binding.noResultLayout.visibility = View.VISIBLE
                        }
                    }
                }
            })
        } else if(feedSort == "feed_bookmark") {
            feedListViewModel.getPagingFeed(
                null, null, null, null,"feed_bookmark", FirebaseAuth.getInstance().currentUser?.email.toString(),
                binding.gridRecyclerview, object : LongTaskCallback<List<Feed>> {
                    override fun onResponse(result: Result<List<Feed>>) {
                        binding.progressBar.visibility = View.GONE
                        binding.gridRecyclerview.visibility = View.VISIBLE
                    }
                }
            )
        }
    }


    private fun feedItemEvent(){
        gridImageAdapter.setOnItemClickListener(object : OnSearchItemClickListener {
            override fun onSearchItemClicked(feed: Feed, position : Int) {
                if(feedSort == "feed_timeline")
                    startActivity<ShowFeedActivity>(
                        "feedSort" to "feed_timeline",
                        "feedPosition" to position,
                        "timeLineEmail" to timeLineEmail
                    )

                else if(feedSort == "feed_bookmark" || feedSort == "feed_search")
                    startActivity<FeedDetailActivity>("feed" to feed)
            }
        })
    }


    private fun observe() {
        feedListViewModel.getFeedListLiveData().observe(viewLifecycleOwner, {
            gridImageAdapter.submitList(it)
        })
    }

    override fun onResume() {
        super.onResume()
    }
}