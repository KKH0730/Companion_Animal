package studio.seno.companion_animal.ui.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import org.jetbrains.anko.startActivity
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.ActivitySearchBinding
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.ui.feed.FeedDetailActivity
import studio.seno.companion_animal.ui.gridLayout.FeedGridFragment
import studio.seno.companion_animal.ui.gridLayout.GridImageAdapter
import studio.seno.companion_animal.ui.feed.FeedListViewModel
import studio.seno.domain.model.Feed
import studio.seno.domain.model.LastSearch
import java.sql.Timestamp

class SearchActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivitySearchBinding
    private val listViewModel: LastSearchListViewModel by viewModels()
    private val feedListViewModel: FeedListViewModel by viewModels()
    private val lastSearchAdapter: LastSearchAdapter by lazy { LastSearchAdapter() }
    private val gridImageAdapter: GridImageAdapter by lazy { GridImageAdapter() }
    private lateinit var feedGridFragment : FeedGridFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)

        binding.lifecycleOwner = this
        binding.listModel = listViewModel
        binding.lastSearchRecyclerView.adapter = lastSearchAdapter

        init()
        lastSearchItemEvent()

        binding.searchBar.setOnEditorActionListener(object : TextView.OnEditorActionListener{
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchButtonEvent()
                    return true
                }
                return false
            }
        })

        listViewModel.requestLoadLastSearch()
        observe()
    }

    private fun init() {
        binding.backBtn.setOnClickListener(this)
    }

    private fun lastSearchItemEvent() {
        lastSearchAdapter.setOnItemClickListener(object : OnLastSearchListener {
            override fun onItemClicked(content: String) {
                binding.searchBar.setText(content)
                searchButtonEvent()
            }

            override fun onDeleteClicked(timestamp: Long, lastSearch: LastSearch) {
                listViewModel.requestDeleteLastSearch(lastSearch)

                var tempList = lastSearchAdapter.currentList.toMutableList()
                tempList.remove(lastSearch)
                listViewModel.setLastSearchLiveData(tempList.toList())
            }
        })

        gridImageAdapter.setOnItemClickListener(object : OnSearchItemClickListener {
            override fun onSearchItemClicked(feed: Feed, position : Int) {
                startActivity<FeedDetailActivity>("feed" to feed)
            }
        })
    }

    private fun observe() {
        listViewModel.getLastSearchLiveData().observe(this, {
            lastSearchAdapter.submitList(it)
        })
    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.back_btn) {
            if(binding.lastSearchRecyclerView.visibility == View.GONE)
                backButtonEvent()
            else
                finish()
        }
    }

    override fun onBackPressed() {
        if(binding.lastSearchRecyclerView.visibility == View.GONE)
                backButtonEvent()
        else
            finish()
    }

    private fun backButtonEvent(){
        binding.searchAnnounce.text = getString(R.string.last_search)
        binding.searchBar.setText("")
         supportFragmentManager.beginTransaction().detach(feedGridFragment).commit()
         binding.lastSearchRecyclerView.visibility = View.VISIBLE
        listViewModel.requestLoadLastSearch()
    }

    private fun searchButtonEvent(){
        feedListViewModel.clearFeedList()
        CommonFunction.closeKeyboard(applicationContext, binding.searchBar)
        val timestamp = Timestamp(System.currentTimeMillis()).time
        val content = binding.searchBar.text.toString().trim()
        binding.searchAnnounce.text = "\"" + content + "\"" + getString(R.string.search_result)

        //검색기록 서버에 저장
        if (content.isNotEmpty()) {
            listViewModel.requestUploadLastSearch(content, timestamp)

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


        binding.lastSearchRecyclerView.visibility = View.GONE
        feedGridFragment = FeedGridFragment.newInstance(content, "feed_search", null)
        supportFragmentManager.beginTransaction().replace(R.id.container, feedGridFragment).commit()
    }
}