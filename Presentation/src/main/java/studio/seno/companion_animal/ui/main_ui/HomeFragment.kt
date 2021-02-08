package studio.seno.companion_animal.ui.main_ui

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import studio.seno.companion_animal.OnItemClickListener
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentHomeBinding
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.ui.feed.FeedListAdapter
import studio.seno.companion_animal.ui.feed.FeedListViewModel
import studio.seno.companion_animal.util.TextUtils.setTextColorBold
import studio.seno.domain.database.InfoManager

/**
 * HomeFragment는 FeedViewListModel과 연결.
 * FeedViewModel는 FeedListAdapter와 연결.
 */
class HomeFragment : Fragment() {
    private lateinit var binding : FragmentHomeBinding
    private val viewModel : FeedListViewModel by viewModels()
    private val feedAdapter : FeedListAdapter by lazy { FeedListAdapter(requireContext(), parentFragmentManager, lifecycle) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_home, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.model = viewModel
        binding.feedRecyclerView.adapter = feedAdapter

        ////게시판 데이터 서버로부터 불러와서 viewmode의 livedata 업데이트
        viewModel.loadFeedList()
        observe()

        itemEvent()
    }
    

    private fun observe(){
        viewModel.getFeedListLiveData().observe(requireActivity(), {
            feedAdapter.submitList(it)
        })
    }

    private fun itemEvent(){
        //댓글작성 버튼클릭
        feedAdapter.setOnItemClickListener(object : OnItemClickListener{
            override fun onItemClicked(commentEdit: EditText, container: LinearLayout) {
                val textView = TextView(context)

                val nickname = InfoManager.getString(requireContext(), "nickName")
                SpannableStringBuilder(nickname).apply {
                    setTextColorBold(this, requireContext(), R.color.black, 0, nickname!!.length)
                    append("  ${commentEdit.text}")
                    textView.text = this
                }

                container.addView(textView)
                commentEdit.setText("")
                commentEdit.hint = requireContext().getString(R.string.comment_hint)
                CommonFunction.closeKeyboard(requireContext(), commentEdit)
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}