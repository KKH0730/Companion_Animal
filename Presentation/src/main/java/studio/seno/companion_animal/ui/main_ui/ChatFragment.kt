package studio.seno.companion_animal.ui.main_ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import org.jetbrains.anko.support.v4.startActivity
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentChatBinding
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.ui.chat.ChatActivity
import studio.seno.companion_animal.ui.chat.ChatAdapter
import studio.seno.companion_animal.ui.chat.ChatListVIewModel
import studio.seno.companion_animal.ui.chat.OnChatItemClickListener
import studio.seno.companion_animal.util.Constants
import studio.seno.datamodule.LocalRepository
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Chat
import studio.seno.domain.model.User
import studio.seno.domain.usecase.remote.ChatUseCase


class ChatFragment : Fragment() {
    private lateinit var  binding : FragmentChatBinding
    private val chatListViewModel : ChatListVIewModel by viewModels()
    private val chatAdapter = ChatAdapter("chat_list")
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.chatListViewModel = chatListViewModel
        binding.chatRecyclerView.adapter = chatAdapter

        init()
        setUserInfo()
        setChatItemEvent()
    }

    fun init(){
        binding.header.findViewById<ImageButton>(R.id.back_btn).visibility = View.GONE
        binding.header.findViewById<TextView>(R.id.title).text = getString(R.string.chat_title1)
    }


    fun setUserInfo(){
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

    fun setChatItemEvent(){
        chatAdapter.setOnChatItemClickListener(object : OnChatItemClickListener{
            override fun onChatItemClicked(chat: Chat, checkDotImage : ImageView) {
                checkDotImage.visibility = View.GONE

                if(chat.email == CommonFunction.getInstance()!!.makeChatPath(FirebaseAuth.getInstance().currentUser?.email.toString())) {
                    startActivity<ChatActivity>(
                        "targetEmail" to chat.targetEmail,
                        "targetProfileUri" to chat.targetProfileUri,
                        "targetNickname" to chat.targetNickname,
                        "targetRealEmail" to chat.targetRealEmail
                    )

                    chatListViewModel.requestUpdateCheckDot(chat.email!!, chat.targetEmail!!)
                } else {
                    startActivity<ChatActivity>(
                        "targetEmail" to chat.email,
                        "targetProfileUri" to chat.profileUri,
                        "targetNickname" to chat.nickname,
                        "targetRealEmail" to chat.realEmail
                    )

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

    fun requestSetChatListListener() {
        chatListViewModel.clearChatList()
        chatListViewModel.requestSetChatListListener(
            CommonFunction.getInstance()!!.makeChatPath(user!!.email),
            "chat_list",
            binding.chatRecyclerView,
            lifecycleScope,
            object : LongTaskCallback<Boolean> {
                override fun onResponse(result: Result<Boolean>) {
                    if (result is Result.Success) {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        )
    }

    fun observe(){
        chatListViewModel.getChatListLiveData().observe(this, {
            chatAdapter.submitList(it)
        })
    }




}