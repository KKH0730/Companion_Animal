package studio.seno.companion_animal.ui.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.startActivity
import studio.seno.commonmodule.CustomToast
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.ActivitySearchBinding
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.ui.feed.FeedDetailActivity
import studio.seno.companion_animal.ui.feed.FeedListViewModel
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Feed
import studio.seno.domain.model.LastSearch
import java.sql.Timestamp

class SearchActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivitySearchBinding
    private val listViewModel: LastSearchListViewModel by viewModels()
    private val feedListViewModel: FeedListViewModel by viewModels()
    private val lastSearchAdapter: LastSearchAdapter by lazy { LastSearchAdapter() }
    private val searchAdapter: SearchResultAdapter by lazy { SearchResultAdapter() }
    private var backKeyPressedTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)

        binding.lifecycleOwner = this
        binding.listModel = listViewModel
        binding.lastSearchRecyclerView.adapter = lastSearchAdapter

        init()
        lastSearchItemEvent()
        listViewModel.requestLoadLastSearch(FirebaseAuth.getInstance().currentUser?.email.toString())
        observe()
    }

    fun init() {
        binding.searchBtn.setOnClickListener(this)
        binding.backBtn.setOnClickListener(this)
    }

    private fun lastSearchItemEvent() {
        lastSearchAdapter.setOnItemClickListener(object : OnLastSearchListener {
            override fun onItemClicked(content: String) {
                binding.searchBar.setText(content)
                searchButtonEvent()
            }

            override fun onDeleteClicked(timestamp: Long, lastSearch: LastSearch) {
                listViewModel.requestDeleteLastSearch(
                    FirebaseAuth.getInstance().currentUser?.email.toString(),
                    lastSearch
                )

                var tempList = lastSearchAdapter.currentList.toMutableList()
                tempList.remove(lastSearch)
                listViewModel.setLastSearchLiveData(tempList.toList())
            }
        })

        searchAdapter.setOnItemClickListener(object : OnSearchItemClickListener {
            override fun onSearchItemClicked(feed: Feed) {
                startActivity<FeedDetailActivity>("feed" to feed)
            }
        })
    }

    private fun observe() {
        listViewModel.getLastSearchLiveData().observe(this, {
            lastSearchAdapter.submitList(it)
        })

        feedListViewModel.getFeedListLiveData().observe(this, {
            searchAdapter.submitList(it)
        })

    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.back_btn) {
            if(binding.lastSearchRecyclerView.layoutManager is StaggeredGridLayoutManager){
                backButtonEvent()
            }else
                finish()

        } else if (v?.id == R.id.search_btn) {
            searchButtonEvent()
        }
    }

    override fun onBackPressed() {
        if(binding.lastSearchRecyclerView.layoutManager is StaggeredGridLayoutManager) {
                backKeyPressedTime = System.currentTimeMillis()
                backButtonEvent()
        } else
            finish()

    }

    fun backButtonEvent(){
        binding.progressBar.visibility = View.GONE
        binding.noResultLayout.visibility = View.GONE
        binding.searchAnnounce.text = getString(R.string.last_search)
        binding.searchBar.setText("")

        binding.lastSearchRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.lastSearchRecyclerView.adapter = lastSearchAdapter
        listViewModel.requestLoadLastSearch(FirebaseAuth.getInstance().currentUser?.email.toString())
    }

    fun searchButtonEvent(){
        CommonFunction.closeKeyboard(applicationContext, binding.searchBar)
        val timestamp = Timestamp(System.currentTimeMillis()).time
        val content = binding.searchBar.text.toString().trim()
        binding.progressBar.visibility = View.VISIBLE
        binding.noResultLayout.visibility = View.GONE
        binding.searchAnnounce.text = "\"" + content + "\"" + getString(R.string.search_result)
        feedListViewModel.clearFeedList()

        //검색기록 서버에 저장
        if (content.isNotEmpty()) {
            listViewModel.requestUploadLastSearch(
                FirebaseAuth.getInstance().currentUser?.email.toString(),
                content,
                timestamp
            )

            var tempList = lastSearchAdapter.currentList.toMutableList()
            var flag = true
            for(element in tempList)
                if(content == element.content)
                    flag = false

            if(flag){
                tempList.add(0, LastSearch(content, timestamp))
                listViewModel.setLastSearchLiveData(tempList.toList())
            }
        }

        //검색 결과를 표시하기 위해 adapter를 교체
        binding.lastSearchRecyclerView.layoutManager =
            StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        binding.lastSearchRecyclerView.adapter = searchAdapter
        binding.lastSearchRecyclerView.scrollToPosition(0)

        //검색 키워드로 검색
        feedListViewModel.requestLoadFeedList(content, binding.lastSearchRecyclerView, object : LongTaskCallback<List<Feed>>{
            override fun onResponse(result: Result<List<Feed>>) {
                binding.progressBar.visibility = View.GONE

                if(result is Result.Success) {
                    if(result.data == null) {
                        binding.noResultTextView.setText("\"" + content + "\"" + getString(R.string.no_result))
                        binding.noResultLayout.visibility = View.VISIBLE
                    }
                }
            }
        })
    }
}