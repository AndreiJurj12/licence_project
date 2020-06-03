package ubb.cscluj.financialforecasting.client

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import ubb.cscluj.financialforecasting.R
import ubb.cscluj.financialforecasting.client.adapter.ClientFeedbackMessageAdapter
import ubb.cscluj.financialforecasting.client.validator.ClientFeedbackMessageValidator
import ubb.cscluj.financialforecasting.client.viewmodel.ClientFeedbackMessageViewModel
import ubb.cscluj.financialforecasting.client.viewmodel.exposed_states.ClientGenericExposedTaskState
import ubb.cscluj.financialforecasting.databinding.FragmentClientFeedbackBinding
import ubb.cscluj.financialforecasting.utils.logd
import ubb.cscluj.financialforecasting.model.FeedbackMessage
import ubb.cscluj.financialforecasting.utils.CustomTextWatcher
import ubb.cscluj.financialforecasting.utils.showSnackbarError
import ubb.cscluj.financialforecasting.utils.showSnackbarSuccessful

class ClientFeedbackFragment : Fragment(), ClientFeedbackMessageAdapter.OnItemClickListener,
    SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentClientFeedbackBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private val clientFeedbackMessageValidator = ClientFeedbackMessageValidator()
    private lateinit var clientFeedbackMessageAdapter: ClientFeedbackMessageAdapter
    private lateinit var clientFeedbackMessageViewModel: ClientFeedbackMessageViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClientFeedbackBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        this.setupRecyclerView()
        this.setupViewModel()

        if (savedInstanceState == null) {
            clientFeedbackMessageViewModel.initialLoading() // first loading
        }

        //setup swipe refresh layout
        binding.swipeRefreshLayout.setOnRefreshListener(this)

        binding.clientFloatingActionButton.setOnClickListener {
            addNewFeedbackMessageClick()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        val viewManager = LinearLayoutManager(activity)
        clientFeedbackMessageAdapter = ClientFeedbackMessageAdapter(this)
        binding.feedbackMessageRecyclerView.apply {
            layoutManager = viewManager
            adapter = clientFeedbackMessageAdapter
        }
    }

    private fun setupViewModel() {
        this.clientFeedbackMessageViewModel =
            ViewModelProvider(this).get(ClientFeedbackMessageViewModel::class.java)
        clientFeedbackMessageViewModel.clientFeedbackMessages.observe(
            viewLifecycleOwner,
            Observer { feedbackMessages ->
                logd("New feedback messages received by observable: $feedbackMessages")
                feedbackMessages.let { clientFeedbackMessageAdapter.updateList(it) }
            })

        clientFeedbackMessageViewModel.clientGenericExposedTaskState.observe(
            viewLifecycleOwner,
            Observer { exposedTaskState ->
                interpretChangeClientGenericExposedTaskState(exposedTaskState)
            })
    }

    private fun interpretChangeClientGenericExposedTaskState(clientGenericExposedTaskState: ClientGenericExposedTaskState) {
        if (clientGenericExposedTaskState.isFinished) {
            binding.loadingProgressBar.hide()
            if (binding.swipeRefreshLayout.isRefreshing) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
            if (clientGenericExposedTaskState.errorReceived) {
                showSnackbarError(clientGenericExposedTaskState.message, binding.coordinatorLayout)
            } else {
                if (clientGenericExposedTaskState.message == "Successful initial loading") {
                    return
                }
                showSnackbarSuccessful(clientGenericExposedTaskState.message, binding.coordinatorLayout)
            }
        }
        else {
            binding.loadingProgressBar.show()
        }
    }

    private fun addNewFeedbackMessageClick() {
        val dialog = MaterialDialog(activity as Activity)
            .cancelOnTouchOutside(false)
            .cancelable(false)
            .noAutoDismiss()
            .customView(R.layout.dialog_add_feedback_message)
            .cornerRadius(16f)
            .lifecycleOwner(viewLifecycleOwner)

        val dialogView = dialog.getCustomView()
        val inputRequest = dialogView.findViewById<TextInputLayout>(R.id.input_request)
        inputRequest.isEndIconVisible = false
        val requestEditText =
            dialogView.findViewById<TextInputEditText>(R.id.message_request_edit_text)
        requestEditText.addTextChangedListener(
            CustomTextWatcher(
                "Please enter a valid request (non-empty)",
                clientFeedbackMessageValidator::validateRequest,
                inputRequest
            )
        )
        dialogView.findViewById<MaterialButton>(R.id.cancel_button)
            .setOnClickListener {
                showSnackbarSuccessful(
                    "Cancelled operation",
                    binding.coordinatorLayout
                )
                dialog.dismiss()
            }
        dialogView.findViewById<MaterialButton>(R.id.send_button)
            .setOnClickListener {
                val requestString = requestEditText.text.toString()
                if (clientFeedbackMessageValidator.validateRequest(requestString)) {
                    clientFeedbackMessageViewModel.addNewUserFeedbackMessage(requestString)
                    dialog.dismiss()
                } else {
                    showSnackbarError(
                        "Invalid request string",
                        binding.coordinatorLayout
                    )
                }
            }
        dialog.show()
    }


    override fun onItemClicked(feedbackMessage: FeedbackMessage) {
        val feedbackMessageId = feedbackMessage.id
        val action =
            ClientFeedbackFragmentDirections.actionClientFeedbackFragmentToClientFeedbackDetailFragment(
                feedbackMessageId
            )
        findNavController().navigate(action)
    }

    override fun onRefresh() {
        clientFeedbackMessageViewModel.refreshFromServer()
    }
}