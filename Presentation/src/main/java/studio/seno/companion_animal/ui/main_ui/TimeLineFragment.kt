package studio.seno.companion_animal.ui.main_ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        binding.menuBtn.setOnClickListener(this)
        binding.nickNameTextView.text = PrefereceManager.getString(requireContext(), "nickName")
    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.menu_Btn) {
            var menuDialog = MenuDialog.newInstance("null", false)
            menuDialog.show(parentFragmentManager, "write")
        }
    }
}