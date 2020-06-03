package ubb.cscluj.financialforecasting.client

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ubb.cscluj.financialforecasting.R
import ubb.cscluj.financialforecasting.client.viewmodel.ClientFeedbackDetailMessageViewModel
import ubb.cscluj.financialforecasting.client.viewmodel.exposed_states.ClientGenericExposedTaskState
import ubb.cscluj.financialforecasting.databinding.FragmentClientFeedbackDetailBinding
import ubb.cscluj.financialforecasting.utils.logd
import ubb.cscluj.financialforecasting.model.FeedbackMessage
import ubb.cscluj.financialforecasting.utils.showSnackbarError
import ubb.cscluj.financialforecasting.utils.showSnackbarSuccessful

class ClientFeedbackDetailFragment : Fragment() {

    private var _binding: FragmentClientFeedbackDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val args: ClientFeedbackDetailFragmentArgs by navArgs()
    private var messageId: Long = -1
    private lateinit var clientFeedbackDetailMessageViewModel: ClientFeedbackDetailMessageViewModel
    private lateinit var feedbackMessage: FeedbackMessage

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClientFeedbackDetailBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        messageId = args.feedbackMessageId
        logd("Received message id: $messageId")

        this.setupViewModel()

        //load the message based on the id
        clientFeedbackDetailMessageViewModel.loadNewFeedbackMessage(messageId)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupViewModel() {
        this.clientFeedbackDetailMessageViewModel =
            ViewModelProvider(this).get(ClientFeedbackDetailMessageViewModel::class.java)

        clientFeedbackDetailMessageViewModel.clientExposedLoadedDetailTaskState.observe(
            viewLifecycleOwner,
            Observer { exposedLoadedDetailTaskState ->
                interpretChangeExposedUpdateTaskState(exposedLoadedDetailTaskState)
            })
    }

    private fun interpretChangeExposedUpdateTaskState(exposedLoadedDetailTaskState: ClientGenericExposedTaskState) {
        if (exposedLoadedDetailTaskState.isFinished) {
            binding.loadingProgressBar.hide()
            if (exposedLoadedDetailTaskState.errorReceived) {
                logd("Received error while initializing feedback message with id $messageId")
                showSnackbarError(
                    exposedLoadedDetailTaskState.message,
                    binding.constraintLayout
                )
                Handler().postDelayed({
                    findNavController().popBackStack()
                }, 500L)
            }
            else {
                showSnackbarSuccessful(
                    exposedLoadedDetailTaskState.message,
                    binding.constraintLayout
                )
                this.feedbackMessage = clientFeedbackDetailMessageViewModel.detailFeedbackMessage.copy()
                this.setupFields()
            }

        }
        else {
            binding.loadingProgressBar.show()
        }
    }

    private fun setupFields() {
        binding.messageRequestTextView.text = this.feedbackMessage.messageRequest

        if (this.feedbackMessage.messageResponse.isNotBlank()) {
            binding.messageResponseTextView.text = this.feedbackMessage.messageResponse
        }
        else {
            binding.messageResponseTextView.text = resources.getText(R.string.no_response_received)
        }
    }
}