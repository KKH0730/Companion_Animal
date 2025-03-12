package studio.seno.companion_animal.ui.main_ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentChatBinding
import studio.seno.companion_animal.extension.startActivity
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.ui.chat.ChatActivity
import studio.seno.companion_animal.ui.chat.ChatAdapter
import studio.seno.companion_animal.ui.chat.ChatListVIewModel
import studio.seno.companion_animal.ui.chat.OnChatItemClickListener
import studio.seno.datamodule.repository.local.LocalRepository
import studio.seno.domain.model.Chat
import studio.seno.domain.model.User
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result

@AndroidEntryPoint
class ChatFragment : Fragment() {
    private var  binding : FragmentChatBinding? = null
    private val chatListViewModel : ChatListVIewModel by viewModels()
    private var chatAdapter : ChatAdapter? = null
    private var user : User? = null

    companion object {
        @JvmStatic
        fun newInstance() =
            ChatFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_chat, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        setUserInfo()
        setChatItemEvent()
    }

    fun init(){
        binding!!.header.findViewById<ImageButton>(R.id.back_btn).visibility = View.GONE
        binding!!.header.findViewById<TextView>(R.id.title).text = getString(R.string.chat_title1)
        chatAdapter = ChatAdapter("chat_list")

        binding!!.lifecycleOwner = viewLifecycleOwner
        binding!!.chatListViewModel = chatListViewModel
        binding!!.chatRecyclerView.adapter = chatAdapter
    }


    private fun setUserInfo(){
        LocalRepository.getInstance(requireContext())!!.getUserInfo(lifecycleScope, object :
            LongTaskCallback<User> {
            override fun onResponse(result: Result<User>) {
                if(result is Result.Success) {
                    user = result.data

                    requestSetChatListListener()
                    observe()

                } else if(result is Result.Error) {
                    Log.e("error", "ChatActivity send_btn error : ${result.exception}")
                }
            }
        })
    }

    private fun setChatItemEvent(){
        chatAdapter!!.setOnChatItemClickListener(object : OnChatItemClickListener{
            override fun onChatItemClicked(chat: Chat, checkDotImage : ImageView) {
                checkDotImage.visibility = View.GONE

                if(chat.email == CommonFunction.getInstance()!!.makeChatPath(FirebaseAuth.getInstance().currentUser?.email.toString())) {
                    requireContext().startActivity(ChatActivity::class.java) {
                        putExtra("targetEmail", chat.targetEmail)
                        putExtra("targetProfileUri", chat.targetProfileUri)
                        putExtra("targetNickname", chat.targetNickname)
                        putExtra("targetRealEmail", chat.targetRealEmail)
                    }

                    chatListViewModel.requestUpdateCheckDot(chat.email!!, chat.targetEmail!!)
                } else {
                    requireContext().startActivity(ChatActivity::class.java) {
                        putExtra("targetEmail", chat.email)
                        putExtra("targetProfileUri", chat.profileUri)
                        putExtra("targetNickname", chat.nickname)
                        putExtra("targetRealEmail", chat.realEmail)
                    }

                   chatListViewModel.requestUpdateCheckDot(chat.targetEmail!!, chat.email!!)
                }
            }

            override fun onExitButtonClicked(chat: Chat) {
                val dialog = AlertDialog.Builder(requireContext(), android.R.style.Theme_DeviceDefault_Light_Dialog)
                dialog.setMessage(getString(R.string.chatDialog_message))
                    .setPositiveButton(getString(R.string.chat_no)) { dialog, _ ->
                        dialog.dismiss()
                    }.setNegativeButton(getString(R.string.chat_yes)) { _, _ ->
                        if(chat.email == CommonFunction.getInstance()!!.makeChatPath(FirebaseAuth.getInstance().currentUser?.email.toString())) {
                            chatListViewModel.requestRemoveChatList(requireContext(), chat.targetEmail!!, chat.email!!, chat.nickname!!, chat)
                        } else {
                            chatListViewModel.requestRemoveChatList(requireContext(), chat.email!!, chat.targetEmail!!, chat.targetNickname!!, chat)
                        }
                    }.show()
            }

            override fun onImageClicked(chat: Chat, position: Int) {
                var email : String? = null
                var targetEmail : String? = null

                if (chat.realEmail == FirebaseAuth.getInstance().currentUser?.email.toString()) {
                    email = chat.email
                    targetEmail = chat.targetEmail
                } else {
                    email = chat.targetEmail
                    targetEmail = chat.email
                }
                chatListViewModel.requestSetAddedChatListener(email!!, targetEmail!!, position, "chat_list", null, null)
            }
        })
    }

    private fun requestSetChatListListener() {
        chatListViewModel.clearChatList()
        chatListViewModel.requestSetChatListListener(
            CommonFunction.getInstance()!!.makeChatPath(user!!.email),
            binding!!.chatRecyclerView,
            lifecycleScope
        )
    }

    private fun observe(){
        chatListViewModel.getChatListLiveData().observe(this, {
            binding?.tvEmpty?.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            chatAdapter!!.submitList(it)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding = null
        chatAdapter = null
    }
}