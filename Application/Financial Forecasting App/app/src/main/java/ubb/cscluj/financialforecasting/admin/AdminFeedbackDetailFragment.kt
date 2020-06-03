package ubb.cscluj.financialforecasting.admin

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ubb.cscluj.financialforecasting.admin.validator.AdminFeedbackValidator
import ubb.cscluj.financialforecasting.admin.viewmodel.exposed_states.AdminExposedLoadedDetailTaskState
import ubb.cscluj.financialforecasting.admin.viewmodel.AdminFeedbackDetailMessageViewModel
import ubb.cscluj.financialforecasting.admin.viewmodel.AdminSharedFeedbackViewModel
import ubb.cscluj.financialforecasting.databinding.FragmentAdminFeedbackDetailBinding
import ubb.cscluj.financialforecasting.model.FeedbackMessage
import ubb.cscluj.financialforecasting.utils.*

class AdminFeedbackDetailFragment : Fragment() {
    private var _binding: FragmentAdminFeedbackDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private val adminSharedFeedbackViewModel: AdminSharedFeedbackViewModel by activityViewModels()
    private val args: AdminFeedbackDetailFragmentArgs by navArgs()
    private var messageId: Long = -1
    private val adminFeedbackValidator = AdminFeedbackValidator()
    private lateinit var adminFeedbackDetailMessageViewModel: AdminFeedbackDetailMessageViewModel
    private lateinit var feedbackMessage: FeedbackMessage

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminFeedbackDetailBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        messageId = args.feedbackMessageId
        logd("Received message id: $messageId")

        this.setupViewModel()
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    this@AdminFeedbackDetailFragment.cancelOperation()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)


        //load the message based on the id
        adminFeedbackDetailMessageViewModel.loadNewFeedbackMessage(messageId)
    }

    private fun setupViewModel() {
        this.adminFeedbackDetailMessageViewModel =
            ViewModelProvider(this).get(AdminFeedbackDetailMessageViewModel::class.java)

        adminFeedbackDetailMessageViewModel.adminExposedLoadedDetailTaskState.observe(
            viewLifecycleOwner,
            Observer { exposedLoadedDetailTaskState ->
                interpretChangeExposedUpdateTaskState(exposedLoadedDetailTaskState)
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun interpretChangeExposedUpdateTaskState(adminExposedLoadedDetailTaskState: AdminExposedLoadedDetailTaskState) {
        if (adminExposedLoadedDetailTaskState.isFinished) {
            binding.loadingProgressBar.hide()
            if (adminExposedLoadedDetailTaskState.errorReceived) {
                showSnackbarError(
                    adminExposedLoadedDetailTaskState.message,
                    binding.constraintLayout
                )
                Handler().postDelayed({
                    this@AdminFeedbackDetailFragment.cancelOperation()
                }, 500L)
            } else {
                showSnackbarSuccessful(
                    adminExposedLoadedDetailTaskState.message,
                    binding.constraintLayout
                )
                this.feedbackMessage = adminFeedbackDetailMessageViewModel.detailFeedbackMessage.copy()
                this.setupFields()
                this.setupUIElements()
            }
        } else {
            binding.loadingProgressBar.show()
        }
    }

    private fun setupFields() {
        binding.messageRequestTextView.text = this.feedbackMessage.messageRequest
        binding.messageResponseEditText.text = this.feedbackMessage.messageResponse.toEditable()
        if (binding.messageResponseEditText.text!!.isNotBlank()) {
            binding.inputResponse.isHintEnabled = false
        }
    }

    private fun setupUIElements() {
        binding.inputResponse.isEndIconVisible = false
        binding.messageResponseEditText.addTextChangedListener(
            CustomTextWatcher(
                "Please enter a valid response (non-empty)",
                adminFeedbackValidator::validateResponse,
                binding.inputResponse
            )
        )
        binding.saveButton.setOnClickListener {
            val response = binding.messageResponseEditText.text.toString().trim()
            if (adminFeedbackValidator.validateResponse(response)) {
                val newFeedbackMessage = feedbackMessage.copy(messageResponse = response)
                adminSharedFeedbackViewModel.addNewUpdateTask(newFeedbackMessage)
                findNavController().popBackStack()
            } else {
                logd("Some field is not valid")
                showSnackbarError(
                    "Some field is not valid",
                    binding.constraintLayout
                )
            }
        }
    }

    private fun cancelOperation() {
        adminSharedFeedbackViewModel.addCancelledUpdateTask()
        findNavController().popBackStack()
    }
}