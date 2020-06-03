package ubb.cscluj.financialforecasting.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ubb.cscluj.financialforecasting.admin.viewmodel.AdminPanelViewModel
import ubb.cscluj.financialforecasting.admin.viewmodel.exposed_states.AdminExposedTaskState
import ubb.cscluj.financialforecasting.databinding.FragmentAdminPanelBinding
import ubb.cscluj.financialforecasting.utils.showSnackbarError
import ubb.cscluj.financialforecasting.utils.showSnackbarSuccessful

class AdminPanelFragment : Fragment() {
    private var _binding: FragmentAdminPanelBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var adminPanelViewModel: AdminPanelViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminPanelBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupViewModel()
        setupUiElements()
    }

    private fun setupViewModel() {
        adminPanelViewModel = ViewModelProvider(this).get(AdminPanelViewModel::class.java)

        adminPanelViewModel.adminExposedTaskState.observe(
            viewLifecycleOwner,
            Observer { exposedTaskState ->
                interpretChangeExposedTaskState(exposedTaskState)
            })
    }

    private fun interpretChangeExposedTaskState(adminExposedTaskState: AdminExposedTaskState) {
        if (adminExposedTaskState.isFinished) {
            binding.loadingProgressBar.hide()
            if (adminExposedTaskState.errorReceived) {
                showSnackbarError(adminExposedTaskState.message, binding.constraintLayout)
            } else {
                showSnackbarSuccessful(adminExposedTaskState.message, binding.constraintLayout)
            }
        }
        else {
            binding.loadingProgressBar.show()
        }
    }

    private fun setupUiElements() {
        binding.updateStockDataButton.setOnClickListener {
            adminPanelViewModel.updateAllStockData()
        }
    }
}