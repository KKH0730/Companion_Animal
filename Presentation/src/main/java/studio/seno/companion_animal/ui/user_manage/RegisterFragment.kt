package studio.seno.companion_animal.ui.user_manage

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import studio.seno.companion_animal.base.CustomToast
import studio.seno.companion_animal.MainActivity
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentRegisterBinding
import studio.seno.companion_animal.extension.startActivity
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.module.TextModule
import studio.seno.companion_animal.util.FinishActivityInterface
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result

@AndroidEntryPoint
class RegisterFragment : Fragment(), View.OnClickListener {
    private var binding: FragmentRegisterBinding? = null
    private val viewModel : UserViewModel by viewModels()
    private var key = false


    private lateinit var finishActivityInterface : FinishActivityInterface

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if(context is FinishActivityInterface)
            finishActivityInterface = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_register,
            container,
            false
        )
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() {
        val textModule = TextModule()
        var ssb = SpannableStringBuilder(binding!!.moveLoginBtn.text.toString())
        ssb = textModule.setTextColorBold(ssb, requireContext(), R.color.red_error, 14, 19).apply {
            binding!!.moveLoginBtn.text = ssb
        }

        binding!!.keyBtn.setOnClickListener(this)
        binding!!.moveLoginBtn.setOnClickListener(this)
        binding!!.registerBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.move_login_btn)
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)

        else if (v?.id == R.id.register_Btn) {
            CommonFunction.closeKeyboard(requireContext(), binding!!.emailInput)

            val email: String = binding!!.emailInput.text.toString().trim()
            val password: String = binding!!.passInput.text.toString().trim()
            val nickName: String = binding!!.nicknameInput.text.toString().trim()



            if (email.isNotEmpty() && nickName.isNotEmpty() && password.isNotEmpty()) {
                viewModel.registerUser(email, password, object : LongTaskCallback<Any> {
                    override fun onResponse(result: Result<Any>) {
                        if (result is Result.Success) {
                            if (result.data as Boolean) {

                                var imageUri = Uri.parse(
                                    ContentResolver.SCHEME_ANDROID_RESOURCE
                                            + "://" + resources.getResourcePackageName(R.drawable.menu_profile)
                                            + '/' + resources.getResourceTypeName(R.drawable.menu_profile)
                                            + '/' + resources.getResourceEntryName(R.drawable.menu_profile)
                                )

                                //회원가입시 기본 프로필 이미지 업로드
                                viewModel.uploadProfileImage(imageUri, object :
                                    LongTaskCallback<Boolean> {
                                    override fun onResponse(result: Result<Boolean>) {
                                        if (result is Result.Success) {

                                            viewModel.loadProfileUri(email, object :
                                                LongTaskCallback<Any> {
                                                override fun onResponse(result: Result<Any>) {
                                                    if (result is Result.Success) {
                                                        val uri = result.data as String

                                                        FirebaseMessaging.getInstance().token.addOnSuccessListener {
                                                            viewModel.requestUploadUserInfo(
                                                                0L,
                                                                email,
                                                                nickName,
                                                                0L,
                                                                0L,
                                                                0L,
                                                                it,
                                                                uri
                                                            )

                                                            requireContext().startActivity(MainActivity::class.java)
                                                            finishActivityInterface.finishCurrentActivity()
                                                        }
                                                    }
                                                }
                                            })
                                        }
                                    }
                                })

                            } else {
                                viewModel.requestCheckOverlapEmail(email, object : LongTaskCallback<Any> {
                                    override fun onResponse(result: Result<Any>) {
                                        if(result is Result.Success) {
                                            if(result.data as Boolean)
                                                CustomToast(
                                                    requireContext(),
                                                    getString(R.string.email_overlap)
                                                ).show()
                                            else
                                                CustomToast(
                                                    requireContext(),
                                                    getString(R.string.register_error)
                                                ).show()
                                        }
                                    }
                                })
                            }
                        } else if (result is Result.Error) {
                            Log.e(
                                "error",
                                "RegisterFragment register user error : ${result.exception}"
                            )
                        }
                    }
                })
            }
        } else if(v?.id == R.id.key_btn) {
            if(key){
                binding!!.passInput.transformationMethod = PasswordTransformationMethod.getInstance()
                key = false
            } else {
                binding!!.passInput.transformationMethod = null
                key = true
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()

        binding = null
    }
}