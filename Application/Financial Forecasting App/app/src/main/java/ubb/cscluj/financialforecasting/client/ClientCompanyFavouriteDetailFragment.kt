package ubb.cscluj.financialforecasting.client

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import ubb.cscluj.financialforecasting.R
import ubb.cscluj.financialforecasting.client.viewmodel.ClientCompanyFavouriteDetailViewModel
import ubb.cscluj.financialforecasting.client.viewmodel.exposed_states.ClientExposedLoadedDetailTaskState
import ubb.cscluj.financialforecasting.databinding.FragmentClientCompanyFavouriteDetailBinding
import ubb.cscluj.financialforecasting.model.Company
import ubb.cscluj.financialforecasting.utils.CustomMarker
import ubb.cscluj.financialforecasting.utils.logd
import ubb.cscluj.financialforecasting.utils.showSnackbarError
import ubb.cscluj.financialforecasting.utils.showSnackbarSuccessful
import java.text.SimpleDateFormat
import java.util.*

class ClientCompanyFavouriteDetailFragment : Fragment() {
    private var _binding: FragmentClientCompanyFavouriteDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val args: ClientCompanySimpleDetailFragmentArgs by navArgs()
    private var companyId = -1L

    private lateinit var clientCompanyFavouriteDetailViewModel: ClientCompanyFavouriteDetailViewModel
    private lateinit var company: Company

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClientCompanyFavouriteDetailBinding.inflate(inflater, container, false)
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
        if (!clientCompanyFavouriteDetailViewModel.checkIfPropertiesAreInitialized()) {
            this.observeViewModelExposedStates()
            clientCompanyFavouriteDetailViewModel.loadNewCompany(companyId)
        } else {
            this.company = clientCompanyFavouriteDetailViewModel.detailCompany.copy()
            this.observeHistoricViewModelExposedState()
            setupFields()
        }
    }

    private fun setupViewModel() {
        this.clientCompanyFavouriteDetailViewModel =
            ViewModelProvider(this).get(ClientCompanyFavouriteDetailViewModel::class.java)
    }

    private fun observeViewModelExposedStates() {
        clientCompanyFavouriteDetailViewModel.clientExposedLoadedDetailTaskState.observe(
            viewLifecycleOwner,
            Observer { exposedLoadedDetailTaskState ->
                interpretChangeExposedLoadedTaskState(exposedLoadedDetailTaskState)
            })
        this.observeHistoricViewModelExposedState()
    }

    private fun observeHistoricViewModelExposedState() {
        clientCompanyFavouriteDetailViewModel.clientExposedLoadedHistoricDateClosePriceTaskState.observe(
            viewLifecycleOwner,
            Observer { exposedLoadedHistoricDateClosePriceTaskState ->
                interpretChangeExposedLoadedHistoricTaskState(
                    exposedLoadedHistoricDateClosePriceTaskState
                )
            }
        )
    }

    private fun interpretChangeExposedLoadedTaskState(clientExposedLoadedDetailTaskState: ClientExposedLoadedDetailTaskState) {
        if (clientExposedLoadedDetailTaskState.isFinished) {
            binding.loadingProgressBar.hide()
            if (clientExposedLoadedDetailTaskState.errorReceived) {
                showSnackbarError(
                    clientExposedLoadedDetailTaskState.message,
                    binding.constraintLayout
                )
                Handler().postDelayed({
                    findNavController().popBackStack()
                }, 500L)
            } else {
                showSnackbarSuccessful(
                    clientExposedLoadedDetailTaskState.message,
                    binding.constraintLayout
                )
                this.company = clientCompanyFavouriteDetailViewModel.detailCompany.copy()
                this.setupFields()
            }
        } else {
            binding.loadingProgressBar.show()
        }
    }

    private fun interpretChangeExposedLoadedHistoricTaskState(
        clientExposedLoadedHistoricDateClosePriceTaskState: ClientExposedLoadedDetailTaskState
    ) {
        if (clientExposedLoadedHistoricDateClosePriceTaskState.isFinished) {
            binding.loadingProgressBar.hide()
            if (clientExposedLoadedHistoricDateClosePriceTaskState.errorReceived) {
                showSnackbarError(
                    clientExposedLoadedHistoricDateClosePriceTaskState.message,
                    binding.constraintLayout
                )
            } else {
                showSnackbarSuccessful(
                    clientExposedLoadedHistoricDateClosePriceTaskState.message,
                    binding.constraintLayout
                )
                this.setupChart()
            }
        } else {
            binding.loadingProgressBar.show()
        }
    }

    private fun setupFields() {
        binding.companyNameTextView.text = this.company.name
        binding.companySymbolTextView.text = this.company.stockTickerSymbol
        binding.companyDescriptionTextView.text = this.company.description
        binding.companyFoundedYearTextView.text =
            getString(R.string.company_founded_year, this.company.foundedYear)
        binding.urlLinkTextView.text = this.company.urlLink
        binding.readyForPredictionToggle.isChecked = this.company.readyForPrediction

        Glide.with(this)
            .load(this.company.urlLogo)
            .placeholder(R.drawable.ic_image)
            .error(R.drawable.ic_broken_image)
            .centerCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.companyLogoImageView)


        binding.historicalPriceChoiceToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                onNewHistoricalPriceRequest(checkedId)
            } else {
                clientCompanyFavouriteDetailViewModel.resetHistoricalClosePrices()
            }

        }

        if (this.company.readyForPrediction) {
            binding.companyPredictionButton.isEnabled = true
            binding.companyPredictionButton.isClickable = true
            binding.companyPredictionButton.isFocusable = true

            binding.companyPredictionButton.setOnClickListener { this.onPredictionButtonClicked() }
        }
    }

    private fun onNewHistoricalPriceRequest(checkedId: Int) {
        when (checkedId) {
            binding.historicalPriceMonthButton.id -> {
                clientCompanyFavouriteDetailViewModel.getHistoricalDataClosePrice(
                    companyId,
                    21
                )
            }
            binding.historicalPriceThreeMonthsButton.id -> {
                clientCompanyFavouriteDetailViewModel.getHistoricalDataClosePrice(
                    companyId,
                    63
                )
            }
            binding.historicalPriceYearButton.id -> {
                clientCompanyFavouriteDetailViewModel.getHistoricalDataClosePrice(
                    companyId,
                    252
                )
            }
            else -> {
                showSnackbarError("An invalid button was toggled?", binding.constraintLayout)
            }
        }

    }

    private fun onPredictionButtonClicked() {
        Toast.makeText(requireActivity(), "Clicked prediction button", Toast.LENGTH_SHORT).show()

        val dialog = MaterialDialog(activity as Activity)
            .cancelOnTouchOutside(false)
            .cancelable(false)
            .cornerRadius(16f)
            .lifecycleOwner(viewLifecycleOwner)

        dialog.show {
            title(R.string.prediction_datepicker_title)
            datePicker { dialog, date ->
                newPredictionRequest(date.time)
            }
            negativeButton {
                showSnackbarError(
                    "Prediction dialog datepicker was dismissed",
                    binding.constraintLayout
                )
            }
        }
    }

    private fun newPredictionRequest(predictionDate: Date) {
        logd("Prediction date $predictionDate")
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val predictionDateString: String = simpleDateFormat.format(predictionDate)

        val action =
            ClientCompanyFavouriteDetailFragmentDirections.actionClientCompanyFavouriteDetailFragmentToClientCompanyPredictionResultFragment(
                company.id,
                predictionDateString
            )
        findNavController().navigate(action)
    }

    private fun setupChart() {
        val historicalPrices =
            clientCompanyFavouriteDetailViewModel.historicalCompanyDateClosePriceList
                .reversed().toList()
        if (historicalPrices.isEmpty()) {
            if (binding.companyHistoricalPriceChart.data != null) {
                binding.companyHistoricalPriceChart.clearValues()
            }
            binding.companyHistoricalPriceChart.clear()
            return
        }


        val colorPrimary = ContextCompat.getColor(
            requireContext(),
            R.color.color_primary
        )
        binding.companyHistoricalPriceChart.apply {
            setTouchEnabled(true)
            setScaleEnabled(true)

            setDrawGridBackground(false)
            maxHighlightDistance = 300f
            isHighlightPerTapEnabled = true

            xAxis.also { x ->
                x.isEnabled = true
                x.granularity = 1f
                x.setDrawGridLines(false)
                x.valueFormatter = object : ValueFormatter() {
                    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                        return historicalPrices[value.toInt()].date
                    }
                }
            }

            axisLeft.also { y ->
                y.textColor = colorPrimary
                y.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
                y.setDrawGridLines(false)
                y.axisLineColor = colorPrimary
                y.typeface = Typeface.DEFAULT_BOLD
            }

            axisRight.isEnabled = false
            legend.isEnabled = false
            description.text = "Historical prices ${historicalPrices.size} days back from ${historicalPrices.last().date}"
            description.textColor = R.color.financialforecasting_yellow_500
            description.typeface = Typeface.DEFAULT


            animateXY(2000, 2000)
        }

        val entries = mutableListOf<Entry>()
        historicalPrices.forEachIndexed { index, dateClosePrice ->
            entries.add(Entry(index.toFloat(), dateClosePrice.closePrice.toFloat()))
        }

        val dataSet = LineDataSet(entries, "Historical Prices").apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
            setDrawFilled(true)
            setDrawCircles(false)
            lineWidth = 1.8f
            circleRadius = 4f
            setCircleColor(colorPrimary)
            highLightColor = Color.rgb(244, 117, 117)
            color = colorPrimary
            fillColor = colorPrimary
            fillAlpha = 50
            valueTextSize = 14f
            setDrawHorizontalHighlightIndicator(false)
            setDrawValues(false)
            setFillFormatter { set, provider ->
                binding.companyHistoricalPriceChart.axisLeft.axisMinimum
            }
        }

        val data = LineData(dataSet)
        binding.companyHistoricalPriceChart.data = data
        binding.companyHistoricalPriceChart.marker =
            CustomMarker(requireContext(), R.layout.marker_view, historicalPrices)
        binding.companyHistoricalPriceChart.invalidate() // refresh chart
    }
}