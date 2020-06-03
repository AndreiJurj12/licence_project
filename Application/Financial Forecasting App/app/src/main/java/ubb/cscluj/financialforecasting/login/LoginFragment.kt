package ubb.cscluj.financialforecasting.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ubb.cscluj.financialforecasting.MainApplication
import ubb.cscluj.financialforecasting.utils.CustomTextWatcher
import ubb.cscluj.financialforecasting.databinding.FragmentLoginBinding
import ubb.cscluj.financialforecasting.utils.logd
import ubb.cscluj.financialforecasting.repository.login.LoginRepository
import ubb.cscluj.financialforecasting.repository.login.LoginResponseException
import ubb.cscluj.financialforecasting.login.validator.LoginValidator
import ubb.cscluj.financialforecasting.model.User
import ubb.cscluj.financialforecasting.utils.showSnackbarError

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private val validator = LoginValidator()
    private lateinit var loginRepository: LoginRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        loginRepository = (activity as LoginActivity).loginRepository

        setupEmailInput()
        setupPasswordInput()
        setupLoginButton()
        setupRegisterLink()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupEmailInput() {
        binding.inputEmail.isEndIconVisible = false
        binding.editTextEmail.addTextChangedListener(
            CustomTextWatcher(
                "Please enter a valid email address",
                validator::validateEmail,
                binding.inputEmail
            )
        )
    }

    private fun setupPasswordInput() {
        binding.editTextPassword.addTextChangedListener(
            CustomTextWatcher(
                "Please enter a valid password (length minimum 8)",
                validator::validatePassword,
                binding.inputPassword,
                endIconInvisibleInGeneral = false
            )
        )
    }

    private fun setupLoginButton() {
        binding.loginButton.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (validator.validateEmail(email) && validator.validatePassword(password)) {
                binding.loadingProgressBar.show()
                CoroutineScope(Dispatchers.IO).launch {
                    val user = User(email, password)
                    try {
                        val loginResponse = loginRepository.loginUser(user)
                        (activity?.application as MainApplication).userToken = loginResponse.userToken
                        (activity?.application as MainApplication).isAdmin = loginResponse.isAdmin
                        (activity?.application as MainApplication).email = user.email
                        (activity?.application as MainApplication).userId = loginResponse.userId

                        activity?.runOnUiThread {
                            binding.loadingProgressBar.hide()
                            goToUserActivity()
                        }
                    } catch (exception: LoginResponseException) {
                        activity?.runOnUiThread {
                            showSnackbarError(
                                exception.message,
                                binding.constraintLayout
                            )
                        }
                    } catch (exception: Exception) {
                        logd("Exception received: ${exception.message}")
                        activity?.runOnUiThread {
                            showSnackbarError(
                                exception.message,
                                binding.constraintLayout
                            )
                        }
                    }
                    finally {
                        activity?.runOnUiThread{
                            binding.loadingProgressBar.hide()
                        }
                    }

                }
            } else {
                logd("Some field is not valid")
                showSnackbarError(
                    "Some field is not valid",
                    binding.constraintLayout
                )
            }
        }
    }

    private fun goToUserActivity() {
        val isAdmin: Boolean = (activity?.application as MainApplication).isAdmin
        val navigationController = findNavController()
        if (isAdmin) {
            val actionNavigateToAdmin =
                LoginFragmentDirections.actionLoginFragmentToAdminActivity()
            navigationController.navigate(actionNavigateToAdmin)
            activity?.finish()
        } else {
            val actionNavigateToClient =
                LoginFragmentDirections.actionLoginFragmentToClientActivity()
            navigationController.navigate(actionNavigateToClient)
            activity?.finish()
        }
    }

    private fun setupRegisterLink() {
        binding.goToRegisterButton.setOnClickListener {
            val actionNavigateToRegister =
                LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            findNavController().navigate(actionNavigateToRegister)
        }
    }
}