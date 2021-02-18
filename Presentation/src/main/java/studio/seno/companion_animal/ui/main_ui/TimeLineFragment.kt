package studio.seno.companion_animal.ui.main_ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentTimeLineBinding
import studio.seno.companion_animal.ui.MenuDialog
import studio.seno.domain.util.PrefereceManager


class TimeLineFragment : Fragment(), View.OnClickListener {
    private lateinit var binding : FragmentTimeLineBinding

    companion object {
        @JvmStatic
        fun newInstance() =
            TimeLineFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_time_line, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    fun init(){
        binding.header.findViewById<ImageButton>(R.id.back_btn).visibility = View.GONE
        binding.header.findViewById<TextView>(R.id.title).text = getString(R.string.timeline_title)
        binding.header.findViewById<LinearLayout>(R.id.menu_set).visibility = View.VISIBLE
        binding.header.findViewById<ImageButton>(R.id.search).visibility = View.GONE
        binding.header.findViewById<ImageButton>(R.id.add).setOnClickListener(this)
        binding.nickNameTextView.text = PrefereceManager.getString(requireContext(), "nickName")
    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.add) {
            var menuDialog = MenuDialog.newInstance("null", false)
            menuDialog.show(parentFragmentManager, "write")
        }
    }
}