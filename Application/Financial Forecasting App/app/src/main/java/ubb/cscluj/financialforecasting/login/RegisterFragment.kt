package ubb.cscluj.financialforecasting.login

import android.os.Bundle
import android.os.Handler
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
import ubb.cscluj.financialforecasting.databinding.FragmentRegisterBinding
import ubb.cscluj.financialforecasting.utils.logd
import ubb.cscluj.financialforecasting.repository.login.LoginRepository
import ubb.cscluj.financialforecasting.repository.login.RegisterResponseException
import ubb.cscluj.financialforecasting.login.validator.RegisterValidator
import ubb.cscluj.financialforecasting.model.network_model.RegisterRequest
import ubb.cscluj.financialforecasting.utils.CustomTextWatcher
import ubb.cscluj.financialforecasting.utils.showSnackbarError
import ubb.cscluj.financialforecasting.utils.showSnackbarSuccessful

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val validator = RegisterValidator()
    private lateinit var loginRepository: LoginRepository


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        loginRepository = (activity as LoginActivity).loginRepository

        setupEmailInput()
        setupPasswordInput()
        setupConfirmPasswordInput()
        setupRegisterButton()
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

    private fun setupConfirmPasswordInput() {
        binding.editTextConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                logd("Text changed")
                if (text != null) {
                    val textString = text.toString().trim()
                    if (validator.validateConfirmPassword(
                            binding.editTextPassword.text.toString().trim(),
                            textString
                        )
                    ) {
                        binding.inputConfirmPassword.isEndIconVisible = true
                        binding.inputConfirmPassword.error = null
                        binding.inputConfirmPassword.isErrorEnabled = false
                    } else {
                        if (textString.isNotEmpty()) {
                            binding.inputConfirmPassword.isErrorEnabled = true
                            binding.inputConfirmPassword.error =
                                "Please confirm your password (length minimum 8)"
                        }
                        else {
                            binding.inputConfirmPassword.isHintEnabled = true
                            binding.inputConfirmPassword.error = null
                            binding.inputConfirmPassword.isErrorEnabled = false
                        }
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                logd("Beginning to change text")
                if (binding.inputConfirmPassword.isHintEnabled)
                    binding.inputConfirmPassword.isHintEnabled = false
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                logd("Input changing")
            }
        })
    }

    private fun setupRegisterButton() {
        binding.registerButton.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()

            if (validator.validateEmail(email) && validator.validatePassword(password) &&
                validator.validateConfirmPassword(password, confirmPassword)
            ) {
                binding.loadingProgressBar.show()
                CoroutineScope(Dispatchers.IO).launch {
                    val registerRequest =
                        RegisterRequest(
                            email,
                            password
                        )
                    try {
                        val registerResponse = loginRepository.registerUser(registerRequest)
                        activity?.runOnUiThread {
                            binding.loadingProgressBar.hide()
                            showSnackbarSuccessful(
                                registerResponse.message,
                                binding.constraintLayout
                            )
                            Handler().postDelayed(
                                {
                                    goBackToLoginFragment()
                                },
                                1000
                            )
                        }
                    } catch (exception: RegisterResponseException) {
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
                    } finally {
                        activity?.runOnUiThread {
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

    private fun goBackToLoginFragment() {
        findNavController().popBackStack()
    }
}
