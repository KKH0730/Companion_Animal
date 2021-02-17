package studio.seno.companion_animal.ui.main_ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentHomeBinding
import studio.seno.companion_animal.databinding.FragmentSearchBinding
import studio.seno.companion_animal.ui.feed.FeedListAdapter
import studio.seno.companion_animal.ui.feed.FeedListViewModel
import studio.seno.companion_animal.ui.search.*
import studio.seno.domain.model.Feed
import studio.seno.domain.model.LastSearch
import java.sql.Timestamp


class SearchFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentSearchBinding
    private val listViewModel: LastSearchListViewModel by viewModels()
    private val feedListViewModel: FeedListViewModel by viewModels()
    private val lastSearchAdapter: LastSearchAdapter by lazy { LastSearchAdapter() }
    private val searchAdapter: SearchResultAdapter by lazy { SearchResultAdapter() }
    companion object {
        @JvmStatic
        fun newInstance() =
            SearchFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_search, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.listModel = listViewModel
        binding.lastSearchRecyclerView.adapter = lastSearchAdapter

        init()
        lastSearchItemEvent()

        listViewModel.requestLoadLastSearch(FirebaseAuth.getInstance().currentUser?.email.toString())
        observe()
    }

    fun init(){
        binding.searchBtn.setOnClickListener(this)
    }

    private fun lastSearchItemEvent(){
        lastSearchAdapter.setOnItemClickListener(object : OnLastSearchListener{
            override fun onItemClicked(content: String) {

            }

            override fun onDeleteClicked(timestamp: Long, lastSearch : LastSearch) {
                listViewModel.requestDeleteLastSearch(
                    FirebaseAuth.getInstance().currentUser?.email.toString(),
                    lastSearch
                )

                var tempList = lastSearchAdapter.currentList.toMutableList()
                tempList.remove(lastSearch)
                listViewModel.setLastSearchLiveData(tempList.toList())
            }
        })

        searchAdapter.setOnItemClickListener(object : OnSearchItemClickListener{
            override fun onSearchItemClicked(feed: Feed) {

            }
        })
    }

    private fun observe() {
        listViewModel.getLastSearchLiveData().observe(requireActivity(), {
            lastSearchAdapter.submitList(it)
        })

        feedListViewModel.getFeedListLiveData().observe(requireActivity(), {
            searchAdapter.submitList(it)
        })

    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.search_btn) {
            val timestamp = Timestamp(System.currentTimeMillis()).time
            val content = binding.searchBar.text.toString().trim()
            binding.searchBar.setText("")

            if(content.isNotEmpty()) {
                listViewModel.requestUploadLastSearch(
                    FirebaseAuth.getInstance().currentUser?.email.toString(),
                    content,
                    timestamp
                )

                var tempList = lastSearchAdapter.currentList.toMutableList()
                tempList.add(0, LastSearch(content, timestamp))
                listViewModel.setLastSearchLiveData(tempList.toList())
            }

            binding.lastSearchRecyclerView.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            binding.lastSearchRecyclerView.adapter = searchAdapter

            feedListViewModel.loadFeedList(null)
        }
    }
}