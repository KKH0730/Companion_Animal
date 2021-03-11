package studio.seno.companion_animal.ui.user_manage

import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.royrodriguez.transitionbutton.TransitionButton
import org.jetbrains.anko.support.v4.startActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import studio.seno.companion_animal.base.CustomToast
import studio.seno.companion_animal.MainActivity
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentLoginBinding
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.module.TextModule
import studio.seno.companion_animal.util.FinishActivityInterface
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result


class LoginFragment : Fragment(), View.OnClickListener {
    private var binding: FragmentLoginBinding? = null
    private val viewModel : UserViewModel by viewModel()
    private lateinit var finishActivityInterface : FinishActivityInterface
    private var key = false

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if(context is FinishActivityInterface)
            finishActivityInterface = context
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_login, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(FirebaseAuth.getInstance().currentUser == null){
            initView()
        } else {
            startActivity<MainActivity>()
            finishActivityInterface.finishCurrentActivity()
        }

    }

    private fun initView() {
        val textModule = TextModule()
        var ssb = SpannableStringBuilder(binding!!.findPasswordBtn.text.toString())
        ssb = textModule.setTextColorBold(ssb, requireContext(), R.color.red_error, 13, 20).apply {
            binding!!.findPasswordBtn.text = ssb
        }

        ssb = SpannableStringBuilder(binding!!.moveRegisterBtn.text.toString())
        ssb = textModule.setTextColorBold(ssb, requireContext(), R.color.red_error, 11, 20).apply {
            binding!!.moveRegisterBtn.text = ssb
        }

        binding!!.findPasswordBtn.setOnClickListener(this)
        binding!!.moveRegisterBtn.setOnClickListener(this)
        binding!!.keyBtn.setOnClickListener(this)
        binding!!.loginBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.findPasswordBtn) {
            findNavController().navigate(R.id.action_loginFragment_to_findPasswordFragment)
        } else if (v?.id == R.id.move_register_btn) {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        } else if (v?.id == R.id.login_Btn) {
            binding!!.loginBtn.startAnimation()
            CommonFunction.closeKeyboard(requireContext(), binding!!.emailInput)

            val email: String = binding!!.emailInput.text.toString().trim()
            val password: String = binding!!.passInput.text.toString().trim()

            if (email.isEmpty()  || password.isEmpty())
                failLogin()
            else {
                viewModel.checkEnableLogin(
                    email,
                    password,
                    object : LongTaskCallback<Any> {
                    override fun onResponse(result: Result<Any>) {
                        if(result is Result.Success) {
                            if(result.data as Boolean) {
                                binding!!.loginBtn.stopAnimation(TransitionButton.StopAnimationStyle.EXPAND) {
                                    startActivity<MainActivity>()
                                    finishActivityInterface.finishCurrentActivity()
                                }
                            } else
                                failLogin()


                        } else if(result is Result.Error) {
                            Log.e("error", "LoginFragment enableLogin error : ${result.exception}")
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

    private fun failLogin(){
        CustomToast(requireContext(), getString(R.string.login_fail)).show()
        binding!!.loginBtn.stopAnimation(TransitionButton.StopAnimationStyle.SHAKE, null)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding = null
    }
}