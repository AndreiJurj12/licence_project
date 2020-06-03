package ubb.cscluj.financialforecasting.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ubb.cscluj.financialforecasting.admin.adapter.AdminCompanyAdapter
import ubb.cscluj.financialforecasting.admin.viewmodel.AdminCompanyViewModel
import ubb.cscluj.financialforecasting.admin.viewmodel.AdminSharedCompanyViewModel
import ubb.cscluj.financialforecasting.admin.viewmodel.exposed_states.AdminExposedAddCompanyTaskState
import ubb.cscluj.financialforecasting.admin.viewmodel.exposed_states.AdminExposedTaskState
import ubb.cscluj.financialforecasting.admin.viewmodel.exposed_states.AdminExposedUpdateCompanyTaskState
import ubb.cscluj.financialforecasting.databinding.FragmentAdminMainBinding
import ubb.cscluj.financialforecasting.utils.logd
import ubb.cscluj.financialforecasting.model.Company
import ubb.cscluj.financialforecasting.utils.showSnackbarError
import ubb.cscluj.financialforecasting.utils.showSnackbarSuccessful

class AdminMainFragment : Fragment(), AdminCompanyAdapter.OnItemClickListener,
    SwipeRefreshLayout.OnRefreshListener {
    private var _binding: FragmentAdminMainBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val adminSharedCompanyViewModel: AdminSharedCompanyViewModel by activityViewModels()

    private lateinit var adminCompanyAdapter: AdminCompanyAdapter
    private lateinit var adminCompanyViewModel: AdminCompanyViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminMainBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                    startActivity(intent)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)


        this.setupRecyclerView()
        this.setupViewModel()
        this.setupFabButton()

        if (savedInstanceState == null) {
            adminCompanyViewModel.initialLoading() //first loading
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
        adminCompanyAdapter = AdminCompanyAdapter(this)
        binding.companyRecyclerView.apply {
            layoutManager = viewManager
            adapter = adminCompanyAdapter
        }
    }

    private fun setupViewModel() {
        this.adminCompanyViewModel =
            ViewModelProvider(this).get(AdminCompanyViewModel::class.java)
        adminCompanyViewModel.allCompanies.observe(
            viewLifecycleOwner,
            Observer { companies ->
                logd("New companies received by observable: $companies")
                companies.let { adminCompanyAdapter.updateList(it) }
            }
        )

        adminCompanyViewModel.adminExposedTaskState.observe(
            viewLifecycleOwner,
            Observer { exposedTaskState ->
                interpretChangeExposedTaskState(exposedTaskState)
            })
    }

    private fun setupExternalTasks() {
        this.adminSharedCompanyViewModel.adminExposedAddCompanyTaskState.observe(
            viewLifecycleOwner,
            Observer { exposedAddCompanyTaskState ->
                interpretChangeExposedAddTaskState(exposedAddCompanyTaskState)
            }
        )

        this.adminSharedCompanyViewModel.adminExposedUpdateCompanyTaskState.observe(
            viewLifecycleOwner,
            Observer { exposedUpdateCompanyTaskState ->
                interpretChangeExposedUpdateTaskState(exposedUpdateCompanyTaskState)
            }
        )
    }

    private fun setupFabButton() {
        binding.adminAddCompanyFloatingActionButton.setOnClickListener {
            val action =
                AdminMainFragmentDirections.actionAdminMainFragmentToAdminCompanyAddDetailFragment()
            findNavController().navigate(action)
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

    private fun interpretChangeExposedAddTaskState(exposedAddCompanyTaskState: AdminExposedAddCompanyTaskState) {
        if (!exposedAddCompanyTaskState.isInitialization) {
            if (exposedAddCompanyTaskState.isCancelled) {
                logd("Cancelled update")
                showSnackbarSuccessful("Addition was not saved", binding.coordinatorLayout)
                adminSharedCompanyViewModel.revertUninitializedAddTask()
            } else {
                logd("Valid addition with new company: ${exposedAddCompanyTaskState.company}")
                val company = exposedAddCompanyTaskState.company as Company
                adminCompanyViewModel.addNewCompany(company)
                adminSharedCompanyViewModel.revertUninitializedAddTask()
            }
        }
    }

    private fun interpretChangeExposedUpdateTaskState(exposedUpdateCompanyTaskState: AdminExposedUpdateCompanyTaskState) {
        if (!exposedUpdateCompanyTaskState.isInitialization) {
            if (exposedUpdateCompanyTaskState.isCancelled) {
                logd("Cancelled update")
                showSnackbarSuccessful("Update was not saved", binding.coordinatorLayout)
                adminSharedCompanyViewModel.revertUninitializedUpdateTask()
            } else {
                logd("Valid update with new company: ${exposedUpdateCompanyTaskState.company}")
                val company = exposedUpdateCompanyTaskState.company as Company
                adminCompanyViewModel.updateCompany(company)
                adminSharedCompanyViewModel.revertUninitializedUpdateTask()
            }
        }
    }

    override fun onItemClicked(company: Company) {
        val action = AdminMainFragmentDirections.actionAdminMainFragmentToAdminCompanyUpdateDetailFragment(
            companyId =  company.id
        )
        findNavController().navigate(action)
    }

    override fun onRefresh() {
        adminCompanyViewModel.refreshFromServer()
    }
}