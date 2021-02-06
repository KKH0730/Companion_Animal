package studio.seno.companion_animal.ui.user_manage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import studio.seno.commonmodule.CustomToast
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentFindPasswordBinding
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.util.ProgressGenerator


class FindPasswordFragment : Fragment(), ProgressGenerator.OnCompleteListener {
    private lateinit var binding : FragmentFindPasswordBinding
    private val progressGenerator by lazy{ProgressGenerator(this)}
    private val viewModel : UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_find_password,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sendEmail.setOnClickListener {
            CommonFunction.closeKeyboard(requireContext(), binding.emailInput)
            var emailAddress = binding.emailInput.text.toString().trim()

            if(emailAddress.isEmpty()) {
                CustomToast(requireContext(), getString(R.string.find_password_announcement1)).show()
            } else {
                viewModel.sendFindEmail(emailAddress)
                viewModel.getFindPasswordListData().observe(requireActivity(), {
                    if (it) {
                        progressGenerator.start(binding.sendEmail)
                        binding.emailInput.isEnabled = false
                        binding.sendEmail.isEnabled = false
                    } else
                        CustomToast(requireContext(), getString(R.string.find_password_announcement1)).show()
                })
            }
        }
    }




    override fun onComplete() {
        CustomToast(requireContext(), getString(R.string.find_password_announcement2)).show()
        findNavController().navigate(R.id.action_findPasswordFragment_to_loginFragment)
    }
}