package studio.seno.companion_animal.ui.main_ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentNotificationBinding
import studio.seno.companion_animal.ui.notification.NotificationListViewModel

class NotificationFragment : Fragment() {
    private lateinit var binding : FragmentNotificationBinding
    private val notiListViewModel : NotificationListViewModel by viewModels()

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
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_notification, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.model = notiListViewModel

    }
}