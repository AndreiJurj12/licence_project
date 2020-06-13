package ubb.cscluj.financialforecasting.client

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ubb.cscluj.financialforecasting.admin.adapter.AdminCompanyAdapter
import ubb.cscluj.financialforecasting.client.adapter.ClientCompanyAdapter
import ubb.cscluj.financialforecasting.client.viewmodel.ClientCompanyViewModel
import ubb.cscluj.financialforecasting.client.viewmodel.exposed_states.ClientGenericExposedTaskState
import ubb.cscluj.financialforecasting.databinding.FragmentClientMainBinding
import ubb.cscluj.financialforecasting.model.Company
import ubb.cscluj.financialforecasting.utils.logd
import ubb.cscluj.financialforecasting.utils.showSnackbarError
import ubb.cscluj.financialforecasting.utils.showSnackbarSuccessful

class ClientMainFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener,
    ClientCompanyAdapter.OnItemClickListener {
    private var _binding: FragmentClientMainBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var clientCompanyAdapter: ClientCompanyAdapter
    private lateinit var clientCompanyViewModel: ClientCompanyViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClientMainBinding.inflate(inflater, container, false)
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


        if (savedInstanceState == null) {
            clientCompanyViewModel.initialLoading() //first loading
        }

        binding.swipeRefreshLayout.setOnRefreshListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        val viewManager = LinearLayoutManager(activity)
        clientCompanyAdapter = ClientCompanyAdapter(this)
        binding.companyRecyclerView.apply {
            layoutManager = viewManager
            adapter = clientCompanyAdapter
        }
    }

    private fun setupViewModel() {
        this.clientCompanyViewModel =
            ViewModelProvider(this).get(ClientCompanyViewModel::class.java)
        clientCompanyViewModel.allCompanies.observe(
            viewLifecycleOwner,
            Observer { companies ->
                logd("New companies received by observable: $companies")
                companies.let { clientCompanyAdapter.updateCompanyList(it) }
            }
        )
        clientCompanyViewModel.allFavouriteCompanies.observe(
            viewLifecycleOwner,
            Observer { favouriteCompanies ->
                logd("New favourite companies received by observable: $favouriteCompanies")
                favouriteCompanies.let { clientCompanyAdapter.updateFavouriteCompanyList(it) }
            }
        )

        clientCompanyViewModel.clientGenericExposedTaskState.observe(
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
                showSnackbarSuccessful(
                    clientGenericExposedTaskState.message,
                    binding.coordinatorLayout
                )
            }
        } else {
            binding.loadingProgressBar.show()
        }
    }

    override fun onRefresh() {
        clientCompanyViewModel.refreshFromServer()
    }

    override fun onItemClicked(company: Company, isFavourite: Boolean) {
        if (isFavourite) {
            logd("Go to favourite company details with id ${company.id}")
            val action = ClientMainFragmentDirections.actionClientMainFragmentToClientCompanyFavouriteDetailFragment(
                company.id
            )
            findNavController().navigate(action)
        } else {
            logd("Go to minimal company details with id ${company.id}")
            val action =
                ClientMainFragmentDirections.actionClientMainFragmentToClientCompanySimpleDetailFragment(
                    company.id
                )
            findNavController().navigate(action)
        }
    }

    override fun onNewFavouriteCompany(newFavouriteCompany: Company) {
        logd("new favourite company to switch with id ${newFavouriteCompany.id}")
        clientCompanyViewModel.switchFavouriteCompany(newFavouriteCompany)
    }
}