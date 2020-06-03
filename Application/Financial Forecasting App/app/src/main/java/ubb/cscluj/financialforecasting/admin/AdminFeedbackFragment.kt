package ubb.cscluj.financialforecasting.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ubb.cscluj.financialforecasting.admin.adapter.AdminFeedbackMessageAdapter
import ubb.cscluj.financialforecasting.admin.viewmodel.AdminFeedbackMessageViewModel
import ubb.cscluj.financialforecasting.admin.viewmodel.AdminSharedFeedbackViewModel
import ubb.cscluj.financialforecasting.admin.viewmodel.exposed_states.AdminExposedTaskState
import ubb.cscluj.financialforecasting.admin.viewmodel.exposed_states.AdminExposedUpdateFeedbackTaskState
import ubb.cscluj.financialforecasting.databinding.FragmentAdminFeedbackBinding
import ubb.cscluj.financialforecasting.utils.logd
import ubb.cscluj.financialforecasting.model.FeedbackMessage
import ubb.cscluj.financialforecasting.utils.showSnackbarError
import ubb.cscluj.financialforecasting.utils.showSnackbarSuccessful

class AdminFeedbackFragment : Fragment(), AdminFeedbackMessageAdapter.OnItemClickListener,
    SwipeRefreshLayout.OnRefreshListener {
    private var _binding: FragmentAdminFeedbackBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val adminSharedFeedbackViewModel: AdminSharedFeedbackViewModel by activityViewModels()

    private lateinit var adminFeedbackMessageAdapter: AdminFeedbackMessageAdapter
    private lateinit var adminFeedbackMessageViewModel: AdminFeedbackMessageViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminFeedbackBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        this.setupRecyclerView()
        this.setupViewModel()

        if (savedInstanceState == null) {
            adminFeedbackMessageViewModel.initialLoading() //first loading
        }

        //setup swipe refresh layout
        binding.swipeRefreshLayout.setOnRefreshListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun setupRecyclerView() {
        val viewManager = LinearLayoutManager(activity)
        adminFeedbackMessageAdapter = AdminFeedbackMessageAdapter(this)
        binding.feedbackMessageRecyclerView.apply {
            layoutManager = viewManager
            adapter = adminFeedbackMessageAdapter
        }
    }

    private fun setupViewModel() {
        this.adminFeedbackMessageViewModel =
            ViewModelProvider(this).get(AdminFeedbackMessageViewModel::class.java)
        adminFeedbackMessageViewModel.allFeedbackMessages.observe(
            viewLifecycleOwner,
            Observer { feedbackMessages ->
                logd("New feedback messages received by observable: $feedbackMessages")
                feedbackMessages.let { adminFeedbackMessageAdapter.updateList(it) }
            })

        adminFeedbackMessageViewModel.adminExposedTaskState.observe(
            viewLifecycleOwner,
            Observer { exposedTaskState ->
                interpretChangeExposedTaskState(exposedTaskState)
            })
    }

    private fun setupExternalTasks() {
        this.adminSharedFeedbackViewModel.adminExposedUpdateFeedbackTaskState.observe(
            viewLifecycleOwner,
            Observer { exposedUpdateTaskState ->
                interpretChangeExposedUpdateTaskState(exposedUpdateTaskState)
            }
        )
    }


    private fun interpretChangeExposedUpdateTaskState(adminExposedUpdateFeedbackTaskState: AdminExposedUpdateFeedbackTaskState) {
        if (!adminExposedUpdateFeedbackTaskState.isInitialization) {
            if (adminExposedUpdateFeedbackTaskState.isCancelled) {
                logd("Cancelled update")
                showSnackbarSuccessful("Update was not saved", binding.coordinatorLayout)
                adminSharedFeedbackViewModel.revertUninitializedUpdateTask()
            } else {
                logd("Valid update with new feedbackmessage: ${adminExposedUpdateFeedbackTaskState.feedbackMessage}")
                val feedbackMessage =
                    adminExposedUpdateFeedbackTaskState.feedbackMessage as FeedbackMessage
                adminFeedbackMessageViewModel.updateFeedbackMessageResponse(feedbackMessage)
                adminSharedFeedbackViewModel.revertUninitializedUpdateTask()
            }
        }
    }

    private fun interpretChangeExposedTaskState(adminExposedTaskState: AdminExposedTaskState) {
        if (adminExposedTaskState.isFinished) {
            binding.loadingProgressBar.hide()
            if (binding.swipeRefreshLayout.isRefreshing) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
            if (adminExposedTaskState.errorReceived) {
                showSnackbarError(adminExposedTaskState.message, binding.coordinatorLayout)
            } else {
                if (adminExposedTaskState.message == "Successful initial loading") {
                    setupExternalTasks()
                    return
                }
                showSnackbarSuccessful(adminExposedTaskState.message, binding.coordinatorLayout)
            }
        }
        else {
            binding.loadingProgressBar.show()
        }
    }


    override fun onItemClicked(feedbackMessage: FeedbackMessage) {
        val feedbackMessageId = feedbackMessage.id
        val action =
            AdminFeedbackFragmentDirections.actionAdminFeedbackFragmentToAdminFeedbackDetailFragment(
                feedbackMessageId
            )
        findNavController().navigate(action)
    }

    override fun onRefresh() {
        adminFeedbackMessageViewModel.refreshFromServer()
    }
}