package studio.seno.companion_animal.ui.chat

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.database.*
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.ActivityChattingBinding
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.module.NotificationModule
import studio.seno.companion_animal.util.Constants
import studio.seno.datamodule.LocalRepository
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Chat
import studio.seno.domain.model.User
import java.sql.Timestamp

class ChatActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding : ActivityChattingBinding
    private lateinit var targetEmail : String
    private lateinit var targetRealEmail : String
    private lateinit var targetProfileUri : String
    private lateinit var targetNickname : String
    private lateinit var notificationModule : NotificationModule
    private val chatListViewModel : ChatListVIewModel by viewModels()
    private val chatAdapter = ChatAdapter("chat")
    private val commonFunction = CommonFunction.getInstance()!!
    private var user : User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chatting)

        binding.chatListViewModel = chatListViewModel
        binding.lifecycleOwner = this
        binding.chatRecyclerview.adapter = chatAdapter


        init()
        setUserInfo()
        setRecyclerPositionListener()
    }

    private fun init(){
        targetEmail = intent.getStringExtra("targetEmail")
        targetRealEmail = intent.getStringExtra("targetRealEmail")
        targetProfileUri = intent.getStringExtra("targetProfileUri")
        targetNickname = intent.getStringExtra("targetNickname")
        binding.content.addTextChangedListener(textWatcher)
        binding.sendBtn.setOnClickListener(this)
        binding.header.findViewById<ImageButton>(R.id.back_btn).setOnClickListener(this)
        binding.header.findViewById<TextView>(R.id.title2).text = targetNickname
    }

    private fun setUserInfo(){
        LocalRepository.getInstance(this)!!.getUserInfo(lifecycleScope, object : LongTaskCallback<User>{
            override fun onResponse(result: Result<User>) {
                if(result is Result.Success) {
                    user = result.data
                    notificationModule = NotificationModule(applicationContext, user!!.nickname)

                    Glide.with(applicationContext)
                        .load(Uri.parse(user!!.profileUri))
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(binding.profileImageVIew)

                    chatListViewModel.requestSetAddedChatListener(commonFunction.makeChatPath(user!!.email), commonFunction.makeChatPath(targetEmail), 0, "chat_log", binding.chatRecyclerview, lifecycleScope)
                    observe()
                } else if(result is Result.Error) {
                    Log.e("error", "ChatActivity send_btn error : ${result.exception}")
                }
            }
        })
    }

    private fun observe(){
        chatListViewModel.getChatListLiveData().observe(this, {
            chatAdapter.submitList(it)
        })
    }

    private fun setRecyclerPositionListener(){
        binding.chatRecyclerview.addOnLayoutChangeListener { v, _, _, _, bottom, _, _, _, oldBottom ->
            if(bottom < oldBottom) {
                v.postDelayed({ binding.chatRecyclerview.smoothScrollToPosition(chatAdapter.itemCount) }, 100)
            }
        }
    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.back_btn){
            finish()
        }else if(v?.id == R.id.send_btn) {
            chatListViewModel.requestAddChat(
                commonFunction.makeChatPath(user!!.email), user!!.email,
                commonFunction.makeChatPath(targetEmail), targetRealEmail,
                user!!.nickname, targetNickname, binding.content.text.toString(),
                user!!.profileUri, targetProfileUri, Timestamp(System.currentTimeMillis()).time
            )

            notificationModule.sendNotification(targetRealEmail, user!!.profileUri, binding.content.text.toString(), Timestamp(System.currentTimeMillis()).time, null, lifecycleScope)
            commonFunction.closeKeyboard(this, binding.content)
            binding.content.setText("")
        }
    }

    private val textWatcher : TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if(binding.content.text.isEmpty())
                binding.sendBtn.visibility = View.INVISIBLE
            else
                binding.sendBtn.visibility = View.VISIBLE
        }

        override fun afterTextChanged(s: Editable?) {}
    }
}