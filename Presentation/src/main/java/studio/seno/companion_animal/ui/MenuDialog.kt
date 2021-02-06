package studio.seno.companion_animal.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.jetbrains.anko.support.v4.startActivity
import studio.seno.commonmodule.CustomToast
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentMenuDialogBinding
import studio.seno.companion_animal.ui.feed.MakeFeedActivity


class MenuDialog : BottomSheetDialogFragment(), View.OnClickListener {
    private lateinit var binding : FragmentMenuDialogBinding
    private var type: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getString("type")
            CustomToast(requireContext(), tag.toString())
        }
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_menu_dialog, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView(){
        binding.showAnimalBtn.setOnClickListener(this)
        binding.questionAnimalBtn.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        if(v?.id == R.id.show_animal_btn) {
            startActivity<MakeFeedActivity>()
            dismiss()

        } else if(v?.id == R.id.question_animal_btn){
            dismiss()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(type : String) =
            MenuDialog().apply {
                arguments = Bundle().apply {
                    putString("type", type)
                }
            }
    }
}