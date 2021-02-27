package studio.seno.companion_animal.ui

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.support.v4.startActivity
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentMenuDialogBinding
import studio.seno.companion_animal.ui.feed.MakeFeedActivity
import studio.seno.domain.util.PreferenceManager


class MenuDialog : BottomSheetDialogFragment(), View.OnClickListener {
    private lateinit var binding : FragmentMenuDialogBinding
    private var email : String? = null
    private var following : Boolean = false

    companion object {
        @JvmStatic
        fun newInstance(email : String, following : Boolean) =
            MenuDialog().apply {
                arguments = Bundle().apply {
                    putString("email", email)
                    putBoolean("following", following)
                }
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            email = it.getString("email")
            following = it.getBoolean("following")
        }
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetStyle)
        isCancelable = true
        PreferenceManager.setString(requireContext(), "mode", "initial_mode")
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
        binding.followBtn.setOnClickListener(this)
        binding.unfollowBtn.setOnClickListener(this)


            binding.menuTitle.text = getString(R.string.menu_setting)

            if(FirebaseAuth.getInstance().currentUser?.email.toString() != email) { //글과 보는사람이 동일하지 않음
                binding.modifyBtn.visibility = View.GONE
                binding.deleteBtn.visibility = View.GONE
                if(following)
                    binding.followBtn.visibility = View.GONE
                else
                    binding.unfollowBtn.visibility = View.GONE

            } else {
                binding.followBtn.visibility = View.GONE
                binding.unfollowBtn.visibility = View.GONE
            }
            binding.makeFeedBtn.visibility = View.GONE
            binding.makeQuestionBtn.visibility = View.GONE

    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.make_feed_btn) {
            startActivity<MakeFeedActivity>()
        } else if(v?.id == R.id.make_question_btn){

        } else if(v?.id == R.id.modify_btn) {
            if(tag == "comment")
                PreferenceManager.setString(requireContext(), "mode", "comment_modify")
            else if(tag == "comment_answer")
                PreferenceManager.setString(requireContext(), "mode", "comment_answer_modify")
            else if(tag == "feed")
                PreferenceManager.setString(requireContext(), "mode", "feed_modify")

        } else if(v?.id == R.id.delete_btn) {
            if(tag == "comment" || tag == "comment_answer")
                PreferenceManager.setString(requireContext(), "mode", "comment_delete")
          else if(tag == "feed")
                PreferenceManager.setString(requireContext(), "mode", "feed_delete")

        } else if(v?.id == R.id.follow_btn){
            PreferenceManager.setString(requireContext(), "mode", "follow")
        } else if(v?.id == R.id.unfollow_btn) {
            PreferenceManager.setString(requireContext(), "mode", "unfollow")
        } else {

        }
        dismiss()
    }



    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        val activity = getActivity()
        if(activity is DialogInterface.OnDismissListener)
            activity.onDismiss(dialog)
    }
}