package studio.seno.companion_animal.ui.main_ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.support.v4.startActivity
import studio.seno.companion_animal.ErrorActivity
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentNotificationBinding
import studio.seno.companion_animal.ui.feed.FeedDetailActivity
import studio.seno.companion_animal.ui.notification.NotificationAdapter
import studio.seno.companion_animal.ui.notification.NotificationListViewModel
import studio.seno.companion_animal.ui.notification.OnNotificationClickedListener
import studio.seno.datamodule.RemoteRepository
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Feed
import studio.seno.domain.model.NotificationData

class NotificationFragment : Fragment() {
    private lateinit var binding : FragmentNotificationBinding
    private val notiListViewModel : NotificationListViewModel by viewModels()
    private val notiAdater: NotificationAdapter =  NotificationAdapter()
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.model = notiListViewModel
        binding.notiRecyclerView.adapter = notiAdater

        init()
        itemEvent()

        notiListViewModel.requestLoadNotification()
        observe()
    }

    private fun init(){
        binding.header.findViewById<ImageButton>(R.id.back_btn).visibility = View.GONE
        binding.header.findViewById<TextView>(R.id.title).text = getString(R.string.notification_title)
    }

    private fun itemEvent(){
        notiAdater.setOnNotificationListener(object : OnNotificationClickedListener{
            override fun onNotificationClicked(notiLayout : ConstraintLayout, item: NotificationData) {
                notiListViewModel.requestUpdateCheckDot(item)

                RemoteRepository.getInstance()!!.loadFeed(item.targetPath!!, object : LongTaskCallback<Feed>{
                    override fun onResponse(result: Result<Feed>) {
                        if(result is Result.Success){
                            if(result.data != null)
                                startActivity<FeedDetailActivity>("feed" to result.data)
                            else
                                startActivity<ErrorActivity>()

                            notiLayout.setBackgroundColor(requireActivity().applicationContext.getColor(R.color.white))
                        } else if(result is Result.Error) {
                            Log.e("error", "NotificationFragment intent error : ${result.exception}")
                        }
                    }
                })

            }

            override fun onDeleteClicked(item: NotificationData) {
                notiListViewModel.deleteNotification(item)
            }
        })
    }

    fun observe(){
        notiListViewModel.getNotificationListLiveData().observe(viewLifecycleOwner, {
            notiAdater.submitList(it)
        })
    }
}