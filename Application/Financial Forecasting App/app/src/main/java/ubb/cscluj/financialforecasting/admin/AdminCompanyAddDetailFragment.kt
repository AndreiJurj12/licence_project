package ubb.cscluj.financialforecasting.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import ubb.cscluj.financialforecasting.admin.validator.AdminCompanyValidator
import ubb.cscluj.financialforecasting.admin.viewmodel.AdminSharedCompanyViewModel
import ubb.cscluj.financialforecasting.databinding.FragmentAdminCompanyAddDetailBinding
import ubb.cscluj.financialforecasting.model.Company
import ubb.cscluj.financialforecasting.utils.CustomTextWatcher
import ubb.cscluj.financialforecasting.utils.logd
import ubb.cscluj.financialforecasting.utils.showSnackbarError

class AdminCompanyAddDetailFragment : Fragment() {
    private var _binding: FragmentAdminCompanyAddDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val adminCompanyValidator: AdminCompanyValidator = AdminCompanyValidator()
    private val adminSharedCompanyViewModel: AdminSharedCompanyViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminCompanyAddDetailBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    this@AdminCompanyAddDetailFragment.cancelOperation()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        setupUIElements()
    }


    private fun setupUIElements() {
        binding.inputName.isEndIconVisible = false
        binding.editTextName.addTextChangedListener(
            CustomTextWatcher(
                invalidName,
                adminCompanyValidator::validateName,
                binding.inputName
            )
        )

        binding.inputSymbol.isEndIconVisible = false
        binding.editTextSymbol.addTextChangedListener(
            CustomTextWatcher(
                invalidStockTickerSymbol,
                adminCompanyValidator::validateSymbol,
                binding.inputSymbol
            )
        )

        binding.inputDescription.isEndIconVisible = false
        binding.editTextDescription.addTextChangedListener(
            CustomTextWatcher(
                invalidDescription,
                adminCompanyValidator::validateDescription,
                binding.inputDescription
            )
        )

        binding.inputFoundedYear.isEndIconVisible = false
        binding.editTextFoundedYear.addTextChangedListener(
            CustomTextWatcher(
                invalidFoundedYear,
                adminCompanyValidator::validateFoundedYear,
                binding.inputFoundedYear
            )
        )

        binding.inputUrlLink.isEndIconVisible = false
        binding.editTextUrlLink.addTextChangedListener(
            CustomTextWatcher(
                invalidUrlLink,
                adminCompanyValidator::validateUrlLink,
                binding.inputUrlLink
            )
        )

        binding.inputUrlLogo.isEndIconVisible = false
        binding.editTextUrlLogo.addTextChangedListener(
            CustomTextWatcher(
                invalidUrlLogo,
                adminCompanyValidator::validateUrlLogo,
                binding.inputUrlLogo
            )
        )

        binding.editTextCsvDataPath.addTextChangedListener(
            CustomTextWatcher(
                invalidCsvDataPath,
                adminCompanyValidator::validateCsvDataPath,
                binding.inputCsvDataPath
            )
        )


        binding.saveButton.setOnClickListener {
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
                val newCompany = Company(
                    name = name,
                    stockTickerSymbol = symbol,
                    description = description,
                    foundedYear = foundedYear.toLong(),
                    urlLink = urlLink,
                    urlLogo = urlLogo,
                    csvDataPath = csvDataPath
                )
                adminSharedCompanyViewModel.addNewAddTask(newCompany)
                findNavController().popBackStack()


            } else {
                logd("Some field is not valid")
                showSnackbarError("Some field is not valid", binding.constraintLayout)
            }
        }
    }

    private fun cancelOperation() {
        adminSharedCompanyViewModel.addCancelledAddTask()
        findNavController().popBackStack()
    }

    companion object ErrorMessages {
        const val invalidName: String = "Invalid name field"
        const val invalidStockTickerSymbol: String = "Invalid stock ticker symbol field"
        const val invalidDescription: String = "Invalid description field"
        const val invalidFoundedYear: String = "Invalid founded year field"
        const val invalidUrlLink: String = "Invalid url link field"
        const val invalidUrlLogo: String = "Invalid url logo field"
        const val invalidCsvDataPath: String = "Invalid csv data path field"
    }
}
