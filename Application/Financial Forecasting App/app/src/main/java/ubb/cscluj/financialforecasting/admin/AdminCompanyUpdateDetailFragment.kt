package ubb.cscluj.financialforecasting.admin

import android.os.Bundle
import android.os.Handler
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
import ubb.cscluj.financialforecasting.admin.validator.AdminCompanyValidator
import ubb.cscluj.financialforecasting.admin.viewmodel.AdminCompanyUpdateDetailViewModel
import ubb.cscluj.financialforecasting.admin.viewmodel.AdminSharedCompanyViewModel
import ubb.cscluj.financialforecasting.admin.viewmodel.exposed_states.AdminExposedLoadedDetailTaskState
import ubb.cscluj.financialforecasting.databinding.FragmentAdminCompanyAddDetailBinding
import ubb.cscluj.financialforecasting.databinding.FragmentAdminCompanyUpdateDetailBinding
import ubb.cscluj.financialforecasting.model.Company
import ubb.cscluj.financialforecasting.utils.*

class AdminCompanyUpdateDetailFragment : Fragment() {
    private var _binding: FragmentAdminCompanyUpdateDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val args: AdminCompanyUpdateDetailFragmentArgs by navArgs()
    private var companyId = -1L

    private val adminCompanyValidator: AdminCompanyValidator = AdminCompanyValidator()
    private val adminSharedCompanyViewModel: AdminSharedCompanyViewModel by activityViewModels()

    private lateinit var adminCompanyUpdateDetailViewModel: AdminCompanyUpdateDetailViewModel
    private lateinit var company: Company

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminCompanyUpdateDetailBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        companyId = args.companyId
        logd("Received company id: $companyId")

        this.setupViewModel()
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    this@AdminCompanyUpdateDetailFragment.cancelOperation()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)


        adminCompanyUpdateDetailViewModel.loadNewCompany(companyId)
    }

    private fun setupViewModel() {
        this.adminCompanyUpdateDetailViewModel =
            ViewModelProvider(this).get(AdminCompanyUpdateDetailViewModel::class.java)

        adminCompanyUpdateDetailViewModel.adminExposedLoadedDetailTaskState.observe(
            viewLifecycleOwner,
            Observer { exposedLoadedDetailTaskState ->
                interpretChangeExposedUpdateTaskState(exposedLoadedDetailTaskState)
            })
    }

    private fun interpretChangeExposedUpdateTaskState(adminExposedLoadedDetailTaskState: AdminExposedLoadedDetailTaskState) {
        if (adminExposedLoadedDetailTaskState.isFinished) {
            binding.loadingProgressBar.hide()
            if (adminExposedLoadedDetailTaskState.errorReceived) {
                showSnackbarError(adminExposedLoadedDetailTaskState.message, binding.constraintLayout)
                Handler().postDelayed({
                    this@AdminCompanyUpdateDetailFragment.cancelOperation()
                }, 500L)
            } else {
                showSnackbarSuccessful(adminExposedLoadedDetailTaskState.message, binding.constraintLayout)
                this.company = adminCompanyUpdateDetailViewModel.detailCompany.copy()
                this.setupFields()
                this.setupUIElements()
            }
        } else {
            binding.loadingProgressBar.show()
        }
    }

    private fun setupFields() {
        binding.editTextName.text = this.company.name.toEditable()
        binding.editTextSymbol.text = this.company.stockTickerSymbol.toEditable()
        binding.editTextDescription.text = this.company.description.toEditable()
        binding.editTextFoundedYear.text = this.company.foundedYear.toString().toEditable()
        binding.editTextUrlLink.text = this.company.urlLink.toEditable()
        binding.editTextUrlLogo.text = this.company.urlLogo.toEditable()
        binding.editTextCsvDataPath.text = this.company.csvDataPath.toEditable()

        binding.readyForPredictionToggle.isChecked = this.company.readyForPrediction
    }

    private fun setupUIElements() {
        binding.editTextName.addTextChangedListener(
            CustomTextWatcher(
                invalidName,
                adminCompanyValidator::validateName,
                binding.inputName
            )
        )

        binding.editTextDescription.addTextChangedListener(
            CustomTextWatcher(
                invalidDescription,
                adminCompanyValidator::validateDescription,
                binding.inputDescription
            )
        )

        binding.editTextFoundedYear.addTextChangedListener(
            CustomTextWatcher(
                invalidFoundedYear,
                adminCompanyValidator::validateFoundedYear,
                binding.inputFoundedYear
            )
        )

        binding.editTextUrlLink.addTextChangedListener(
            CustomTextWatcher(
                invalidUrlLink,
                adminCompanyValidator::validateUrlLink,
                binding.inputUrlLink
            )
        )

        binding.editTextUrlLogo.addTextChangedListener(
            CustomTextWatcher(
                invalidUrlLogo,
                adminCompanyValidator::validateUrlLogo,
                binding.inputUrlLogo
            )
        )


        binding.updateButton.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val symbol = binding.editTextSymbol.text.toString().trim()
            val description = binding.editTextDescription.text.toString().trim()
            val foundedYear = binding.editTextFoundedYear.text.toString().trim()
            val urlLink = binding.editTextUrlLink.text.toString().trim()
            val urlLogo = binding.editTextUrlLogo.text.toString().trim()
            val csvDataPath = binding.editTextCsvDataPath.text.toString().trim()
            if (adminCompanyValidator.validateName(name) &&
                adminCompanyValidator.validateSymbol(symbol) &&
                adminCompanyValidator.validateDescription(description) &&
                adminCompanyValidator.validateFoundedYear(foundedYear) &&
                adminCompanyValidator.validateUrlLink(urlLink) &&
                adminCompanyValidator.validateUrlLogo(urlLogo) &&
                adminCompanyValidator.validateCsvDataPath(csvDataPath)
            ) {
                val readyForPrediction: Boolean = binding.readyForPredictionToggle.isChecked

                val newCompany = Company(
                    name = name,
                    stockTickerSymbol = symbol,
                    description = description,
                    foundedYear = foundedYear.toLong(),
                    urlLink = urlLink,
                    urlLogo = urlLogo,
                    csvDataPath = csvDataPath,
                    readyForPrediction = readyForPrediction
                )
                newCompany.id = companyId

                adminSharedCompanyViewModel.addNewUpdateTask(newCompany)
                findNavController().popBackStack()


            } else {
                logd("Some field is not valid")
                showSnackbarError("Some field is not valid", binding.constraintLayout)
            }
        }
    }

    private fun cancelOperation() {
        adminSharedCompanyViewModel.addCancelledUpdateTask()
        findNavController().popBackStack()
    }

    companion object ErrorMessages {
        const val invalidName: String = "Invalid name field"
        const val invalidDescription: String = "Invalid description field"
        const val invalidFoundedYear: String = "Invalid founded year field"
        const val invalidUrlLink: String = "Invalid url link field"
        const val invalidUrlLogo: String = "Invalid url logo field"
    }
}
