package studio.seno.companion_animal.ui

import android.app.Activity
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.support.v4.startActivity
import studio.seno.commonmodule.CustomToast
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentMenuDialogBinding
import studio.seno.companion_animal.ui.feed.MakeFeedActivity
import studio.seno.domain.database.InfoManager


class MenuDialog : BottomSheetDialogFragment(), View.OnClickListener {
    private lateinit var binding : FragmentMenuDialogBinding
    private var email : String? = null

    companion object {
        @JvmStatic
        fun newInstance(email : String) =
            MenuDialog().apply {
                arguments = Bundle().apply {
                    putString("email", email)
                }
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            email = it.getString("email")
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
        binding.makeFeedBtn.setOnClickListener(this)
        binding.makeQuestionBtn.setOnClickListener(this)
        binding.modifyBtn.setOnClickListener(this)
        binding.deleteBtn.setOnClickListener(this)
        binding.reportBtn.setOnClickListener(this)

        if(tag == "write") {
            binding.menuTitle.text = getString(R.string.menu_title)
            binding.modifyBtn.visibility = View.GONE
            binding.deleteBtn.visibility = View.GONE
            binding.reportBtn.visibility = View.GONE
        } else {
            binding.menuTitle.text = getString(R.string.menu_setting)

            if(FirebaseAuth.getInstance().currentUser?.email.toString() != email) {
                binding.modifyBtn.visibility = View.GONE
                binding.deleteBtn.visibility = View.GONE
            }
            binding.makeFeedBtn.visibility = View.GONE
            binding.makeQuestionBtn.visibility = View.GONE
        }
    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.make_feed_btn) {
            startActivity<MakeFeedActivity>()
            dismiss()
        } else if(v?.id == R.id.make_question_btn){
            dismiss()

        } else if(v?.id == R.id.modify_btn) {
            if(tag == "comment")
                InfoManager.setString(requireContext(), "mode", "comment_modify")
            else if(tag == "comment_answer")
                InfoManager.setString(requireContext(), "mode", "comment_answer_modify")
            else if(tag == "feed")
                InfoManager.setString(requireContext(), "mode", "feed_modify")
            dismiss()
        } else if(v?.id == R.id.delete_btn) {
            if(tag == "comment")
                InfoManager.setString(requireContext(), "mode", "comment_delete")
            else if(tag == "comment_answer")
                InfoManager.setString(requireContext(), "mode", "comment_delete")
            else if(tag == "feed")
                InfoManager.setString(requireContext(), "mode", "feed_delete")
            dismiss()
        } else {

        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        val activity = getActivity()
        if(activity is DialogInterface.OnDismissListener)
            activity.onDismiss(dialog)
    }
}