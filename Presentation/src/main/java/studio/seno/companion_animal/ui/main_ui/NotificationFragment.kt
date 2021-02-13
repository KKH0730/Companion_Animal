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
import com.google.firebase.auth.FirebaseAuth
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentNotificationBinding
import studio.seno.companion_animal.ui.feed.FeedListAdapter
import studio.seno.companion_animal.ui.notification.NotificationAdapter
import studio.seno.companion_animal.ui.notification.NotificationListViewModel

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
        notiListViewModel.requestLoadNotification(FirebaseAuth.getInstance().currentUser?.email.toString())
        observe()



    }

    fun observe(){
        notiListViewModel.getNotificationListLiveData().observe(viewLifecycleOwner, {
            notiAdater.submitList(it)
        })
    }
}