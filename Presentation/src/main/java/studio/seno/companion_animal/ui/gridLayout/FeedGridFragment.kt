package studio.seno.companion_animal.ui.gridLayout

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentFeedGridBinding
import studio.seno.companion_animal.extension.startActivity
import studio.seno.companion_animal.ui.feed.FeedDetailActivity
import studio.seno.companion_animal.ui.feed.FeedListViewModel
import studio.seno.companion_animal.ui.feed.ShowFeedActivity
import studio.seno.companion_animal.ui.search.OnSearchItemClickListener
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result
import studio.seno.domain.model.Feed

@AndroidEntryPoint
class FeedGridFragment : Fragment() {
    private val feedListViewModel: FeedListViewModel by viewModels()
    private var binding : FragmentFeedGridBinding? = null
    private var gridImageAdapter : GridImageAdapter? = null
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
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        setFeedList()
        feedItemEvent()
        observe()
    }

    private fun init(){

        binding!!.progressBar.visibility = View.VISIBLE
        gridImageAdapter = GridImageAdapter()

        binding!!.lifecycleOwner = viewLifecycleOwner
        binding!!.feedListViewModel = feedListViewModel
        binding!!.gridRecyclerview.adapter = gridImageAdapter
    }

    private fun setFeedList() {
        feedListViewModel.clearFeedList()


        if(feedSort == "feed_timeline") {
            feedListViewModel.getPagingFeed(
                null, null, null,
                null,
                "feed_timeline",
                timeLineEmail,
                binding!!.gridRecyclerview,
                object : LongTaskCallback<Any> {
                    override fun onResponse(result: Result<Any>) {
                        binding!!.progressBar.visibility = View.GONE
                        binding!!.gridRecyclerview.visibility = View.VISIBLE
                    }
                }
            )
        } else if(feedSort == "feed_search"){
            feedListViewModel.getPagingFeed(
                null, null, null,
                keyword,
                "feed_search",
                null,
                binding!!.gridRecyclerview,
                object : LongTaskCallback<Any> {
                override fun onResponse(result: Result<Any>) {
                    binding!!.progressBar.visibility = View.GONE
                    binding!!.gridRecyclerview.visibility = View.VISIBLE
                    if(result is Result.Success) {
                        if(result.data == null) {
                            binding!!.noResultTextView.setText("\"" + keyword + "\"" + getString(R.string.no_result))
                            binding!!.noResultLayout.visibility = View.VISIBLE
                        }
                    }
                }
            })
        } else if(feedSort == "feed_bookmark" || feedSort == "feed_timeline") {
            feedListViewModel.getPagingFeed(
                null, null, null, null,"feed_bookmark", FirebaseAuth.getInstance().currentUser?.email.toString(),
                binding!!.gridRecyclerview, object : LongTaskCallback<Any> {
                    override fun onResponse(result: Result<Any>) {
                        binding!!.progressBar.visibility = View.GONE
                        binding!!.gridRecyclerview.visibility = View.VISIBLE
                    }
                }
            )
        }
    }


    private fun feedItemEvent(){
        gridImageAdapter!!.setOnItemClickListener(object : OnSearchItemClickListener {
            override fun onSearchItemClicked(feed: Feed, position : Int) {
                if(feedSort == "feed_timeline")
                    requireContext().startActivity(ShowFeedActivity::class.java) {
                        putExtra("feedSort" , "feed_timeline")
                        putExtra("feedPosition" , position)
                        putExtra("timeLineEmail" , timeLineEmail)
                    }

                else if(feedSort == "feed_bookmark" || feedSort == "feed_search")
                    requireContext().startActivity(FeedDetailActivity::class.java) {
                        putExtra("feed", feed)
                    }
            }
        })
    }


    private fun observe() {
        feedListViewModel.getFeedListLiveData().observe(viewLifecycleOwner, {
            gridImageAdapter!!.submitList(it)
        })
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding = null
        gridImageAdapter = null
    }
}