package studio.seno.companion_animal.ui.main_ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import dagger.hilt.android.AndroidEntryPoint
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentNotificationBinding
import studio.seno.companion_animal.extension.startActivity
import studio.seno.companion_animal.ui.ReportActivity
import studio.seno.companion_animal.ui.feed.FeedDetailActivity
import studio.seno.companion_animal.ui.notification.NotificationAdapter
import studio.seno.companion_animal.ui.notification.NotificationListViewModel
import studio.seno.companion_animal.ui.notification.OnNotificationClickedListener
import studio.seno.domain.model.Feed
import studio.seno.domain.model.NotificationData
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result

@AndroidEntryPoint
class NotificationFragment : Fragment() {
    private var binding : FragmentNotificationBinding? = null
    private val notificationListViewModel : NotificationListViewModel by viewModels()
    private var notificationAdapter: NotificationAdapter? =  null

    companion object {
        @JvmStatic
        fun newInstance() =
            NotificationFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_notification, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        itemEvent()

        notificationListViewModel.requestLoadNotification()
        observe()
    }

    private fun init(){
        binding!!.header.findViewById<ImageButton>(R.id.back_btn).visibility = View.GONE
        binding!!.header.findViewById<TextView>(R.id.title).text = getString(R.string.notification_title)
        notificationAdapter = NotificationAdapter()

        binding!!.lifecycleOwner = this
        binding!!.model = notificationListViewModel
        binding!!.notiRecyclerView.adapter = notificationAdapter

    }

    private fun itemEvent(){
        notificationAdapter!!.setOnNotificationListener(object : OnNotificationClickedListener{
            override fun onNotificationClicked(notificationLayout : ConstraintLayout, item: NotificationData) {
                notificationListViewModel.requestUpdateCheckDot(item)

                notificationListViewModel.getFeed(item.feedPath!!, object :
                    LongTaskCallback<Any> {
                    override fun onResponse(result: Result<Any>) {
                        if(result is Result.Success){
                            if(result.data != null)
                                requireContext().startActivity(FeedDetailActivity::class.java) {
                                    putExtra("feed", result.data as Feed)
                                }
                            else
                                requireContext().startActivity(ReportActivity::class.java)

                            notificationLayout.setBackgroundColor(requireActivity().applicationContext.getColor(R.color.white))
                        } else if(result is Result.Error) {
                            Log.e("error", "NotificationFragment intent error : ${result.exception}")
                        }
                    }
                })
            }

            override fun onDeleteClicked(item: NotificationData) {
                notificationListViewModel.deleteNotification(item)
            }
        })
    }

    private fun observe(){
        notificationListViewModel.getNotificationListLiveData().observe(viewLifecycleOwner, {
            binding?.tvEmpty?.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            notificationAdapter!!.submitList(it)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding = null
        notificationAdapter = null
    }
}