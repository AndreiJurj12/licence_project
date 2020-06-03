package ubb.cscluj.financialforecasting.client

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ubb.cscluj.financialforecasting.MainApplication
import ubb.cscluj.financialforecasting.repository.logout.LogoutRepository
import ubb.cscluj.financialforecasting.databinding.FragmentClientUserProfileBinding
import ubb.cscluj.financialforecasting.utils.logd
import ubb.cscluj.financialforecasting.model.network_model.LogoutRequest
import ubb.cscluj.financialforecasting.utils.showSnackbarError
import ubb.cscluj.financialforecasting.utils.showSnackbarSuccessful
import java.lang.Exception

class ClientProfileFragment : Fragment() {
    private var _binding: FragmentClientUserProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var logoutRepository: LogoutRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClientUserProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //setup logout repository ready
        logoutRepository =
            LogoutRepository((activity?.application as MainApplication).networkService)


        //setup username inside our textview
        binding.userEmailTextView.text = (activity?.application as MainApplication).email
        this.setupLogoutButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupLogoutButton() {
        binding.logoutButton.setOnClickListener {
            binding.loadingProgressBar.show()
            val userToken = (activity as ClientActivity).userToken
            CoroutineScope(Dispatchers.IO).launch {
                val logoutRequest = LogoutRequest(userToken = userToken)
                try {
                    val logoutResponse = logoutRepository.logoutUser(logoutRequest)
                    activity?.runOnUiThread {
                        binding.loadingProgressBar.hide()
                        showSnackbarSuccessful(
                            logoutResponse.message,
                            binding.constraintLayout
                        )
                    }
                } catch (exception: Exception) {
                    logd("Exception received: ${exception.message}")
                    activity?.runOnUiThread {
                        binding.loadingProgressBar.hide()
                        showSnackbarError(
                            exception.message,
                            binding.constraintLayout
                        )
                    }
                } finally {
                    CoroutineScope(Dispatchers.Main).launch {
                        Handler().postDelayed({
                            (activity as ClientActivity).logoutUser()
                        }, 1000)
                    }
                }
            }
        }
    }
}