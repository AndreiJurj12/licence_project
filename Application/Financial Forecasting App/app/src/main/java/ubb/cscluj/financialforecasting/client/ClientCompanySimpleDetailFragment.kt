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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import ubb.cscluj.financialforecasting.R
import ubb.cscluj.financialforecasting.client.viewmodel.ClientCompanySimpleDetailViewModel
import ubb.cscluj.financialforecasting.client.viewmodel.exposed_states.ClientExposedLoadedDetailTaskState
import ubb.cscluj.financialforecasting.databinding.FragmentClientCompanySimpleDetailBinding
import ubb.cscluj.financialforecasting.model.Company
import ubb.cscluj.financialforecasting.utils.logd
import ubb.cscluj.financialforecasting.utils.showSnackbarError
import ubb.cscluj.financialforecasting.utils.showSnackbarSuccessful

class ClientCompanySimpleDetailFragment : Fragment() {
    private var _binding: FragmentClientCompanySimpleDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val args: ClientCompanyFavouriteDetailFragmentArgs by navArgs()
    private var companyId = -1L

    private lateinit var clientCompanySimpleDetailViewModel: ClientCompanySimpleDetailViewModel
    private lateinit var company: Company

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClientCompanySimpleDetailBinding.inflate(inflater, container, false)
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


        if (!clientCompanySimpleDetailViewModel.checkIfPropertiesAreInitialized()) {
            observeViewModelExposedStates()
            clientCompanySimpleDetailViewModel.loadNewCompany(companyId)
        } else {
            this.company = clientCompanySimpleDetailViewModel.detailCompany.copy()
            setupFields()
        }
    }

    private fun setupViewModel() {
        this.clientCompanySimpleDetailViewModel =
            ViewModelProvider(this).get(ClientCompanySimpleDetailViewModel::class.java)
    }

    private fun observeViewModelExposedStates() {
        clientCompanySimpleDetailViewModel.clientExposedLoadedDetailTaskState.observe(
            viewLifecycleOwner,
            Observer { exposedLoadedDetailTaskState ->
                interpretChangeExposedLoadedTaskState(exposedLoadedDetailTaskState)
            })
    }

    private fun interpretChangeExposedLoadedTaskState(clientExposedLoadedDetailTaskState: ClientExposedLoadedDetailTaskState) {
        if (clientExposedLoadedDetailTaskState.isFinished) {
            binding.loadingProgressBar.hide()
            if (clientExposedLoadedDetailTaskState.errorReceived) {
                showSnackbarError(clientExposedLoadedDetailTaskState.message, binding.constraintLayout)
                Handler().postDelayed({
                    findNavController().popBackStack()
                }, 500L)
            } else {
                showSnackbarSuccessful(clientExposedLoadedDetailTaskState.message, binding.constraintLayout)
                this.company = clientCompanySimpleDetailViewModel.detailCompany.copy()
                this.setupFields()
            }
        } else {
            binding.loadingProgressBar.show()
        }
    }

    private fun setupFields() {
        binding.companyNameTextView.text = this.company.name
        binding.companySymbolTextView.text = this.company.stockTickerSymbol
        binding.companyDescriptionTextView.text = this.company.description
        binding.companyFoundedYearTextView.text = getString(R.string.company_founded_year, this.company.foundedYear)
        binding.urlLinkTextView.text = this.company.urlLink
        binding.readyForPredictionToggle.isChecked = this.company.readyForPrediction

        Glide.with(this)
            .load(this.company.urlLogo)
            .placeholder(R.drawable.ic_image)
            .error(R.drawable.ic_broken_image)
            .centerCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.companyLogoImageView)
    }
}