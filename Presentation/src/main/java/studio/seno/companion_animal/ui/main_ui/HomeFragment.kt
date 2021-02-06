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
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentHomeBinding
import studio.seno.companion_animal.ui.feed.FeedListAdapter
import studio.seno.companion_animal.ui.feed.FeedListViewModel


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

    }



    private fun observe(){
        viewModel.getFeedListLiveData().observe(requireActivity(), {
            feedAdapter.submitList(it)
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