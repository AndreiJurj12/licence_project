package ubb.cscluj.financialforecasting.client

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.view.marginRight
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import kotlinx.android.synthetic.main.fragment_admin_company_add_detail.*
import ubb.cscluj.financialforecasting.R
import ubb.cscluj.financialforecasting.client.viewmodel.ClientCompanyPredictionResultViewModel
import ubb.cscluj.financialforecasting.client.viewmodel.exposed_states.ClientExposedLoadedDetailTaskState
import ubb.cscluj.financialforecasting.databinding.FragmentClientCompanyPredictionResultBinding
import ubb.cscluj.financialforecasting.model.Company
import ubb.cscluj.financialforecasting.model.network_model.DateClosePrice
import ubb.cscluj.financialforecasting.utils.CustomMarker
import ubb.cscluj.financialforecasting.utils.logd
import ubb.cscluj.financialforecasting.utils.showSnackbarError
import ubb.cscluj.financialforecasting.utils.showSnackbarSuccessful
import java.text.SimpleDateFormat

class ClientCompanyPredictionResultFragment : Fragment() {
    private var _binding: FragmentClientCompanyPredictionResultBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val args: ClientCompanyPredictionResultFragmentArgs by navArgs()

    private var companyId = -1L
    private var predictionStartingDate = ""

    private lateinit var clientCompanyPredictionResultViewModel: ClientCompanyPredictionResultViewModel
    private lateinit var company: Company

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClientCompanyPredictionResultBinding.inflate(inflater, container, false)
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
        predictionStartingDate = args.predictionStartingDate
        logd("Received company id: $companyId")

        this.setupViewModel()

        if (!clientCompanyPredictionResultViewModel.checkIfPropertiesAreInitialized()) {
            observeViewModelExposedStates()
            clientCompanyPredictionResultViewModel.loadNewCompany(companyId)
        } else {
            this.company = clientCompanyPredictionResultViewModel.detailCompany.copy()
            setupInitialFields()
            setupPredictionFields()
            setupHistoricalPriceChart()
        }
    }

    private fun setupViewModel() {
        this.clientCompanyPredictionResultViewModel =
            ViewModelProvider(this).get(ClientCompanyPredictionResultViewModel::class.java)
    }

    private fun observeViewModelExposedStates() {
        clientCompanyPredictionResultViewModel.clientExposedLoadedDetailTaskState.observe(
            viewLifecycleOwner,
            Observer { exposedLoadedDetailTaskState ->
                interpretChangeExposedLoadedTaskState(exposedLoadedDetailTaskState)
            })
        clientCompanyPredictionResultViewModel.clientExposedLoadedPredictionTaskState.observe(
            viewLifecycleOwner,
            Observer { exposedLoadedPredictionTaskState ->
                interpretChangeExposedLoadedPredictionTaskState(exposedLoadedPredictionTaskState)
            })
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
                }, 3000L)
            } else {
                showSnackbarSuccessful(
                    clientExposedLoadedDetailTaskState.message,
                    binding.constraintLayout
                )
                this.company = clientCompanyPredictionResultViewModel.detailCompany.copy()
                this.setupInitialFields()
                this.requestNewPrediction()
            }
        } else {
            binding.loadingProgressBar.show()
        }
    }

    private fun setupInitialFields() {
        binding.companyNameTextView.text = this.company.name
        binding.companySymbolTextView.text = this.company.stockTickerSymbol
        binding.companyPredictionDateTextView.text =
            getString(R.string.prediction_for_date, this.predictionStartingDate)
    }

    private fun requestNewPrediction() {
        clientCompanyPredictionResultViewModel.requirePrediction(companyId, predictionStartingDate)
    }

    private fun interpretChangeExposedLoadedPredictionTaskState(
        clientExposedLoadedPredictionTaskState: ClientExposedLoadedDetailTaskState
    ) {
        if (clientExposedLoadedPredictionTaskState.isFinished) {
            binding.loadingProgressBar.hide()
            if (clientExposedLoadedPredictionTaskState.errorReceived) {
                showSnackbarError(
                    clientExposedLoadedPredictionTaskState.message,
                    binding.constraintLayout
                )
                Handler().postDelayed({
                    findNavController().popBackStack()
                }, 3000L)
            } else {
                showSnackbarSuccessful(
                    clientExposedLoadedPredictionTaskState.message,
                    binding.constraintLayout
                )
                this.setupPredictionFields()
                this.setupHistoricalPriceChart()
            }
        } else {
            binding.loadingProgressBar.show()
        }
    }

    private fun setupPredictionFields() {
        val predictionResponseDto = clientCompanyPredictionResultViewModel.predictionResponseDto


        // closing prices are in reversed order (from predictionstartingdate to past)
        val lastClosingPrice = predictionResponseDto.historicalClosePrice.first().closePrice
        binding.companyLastClosingPriceTextView.text =
            getString(R.string.prediction_for_last_closing_price, lastClosingPrice.toFloat())

        // expected prediction price if known
        var expectedPredictionPrice = "unknown"
        if (predictionResponseDto.expectedPredictedPrice != null)
            expectedPredictionPrice = predictionResponseDto.expectedPredictedPrice.closePrice.toString()
        binding.companyExpectedPredictionTestView.text =
            getString(R.string.expected_prediction_for_closing_price, expectedPredictionPrice)

        //setup classification
        val classificationString = predictionResponseDto.classificationResult
        binding.companyClassificationResultTextView.text = classificationString
        if (classificationString == increaseClassificationString) {
            binding.classificationImageView.setImageResource(R.drawable.ic_trending_up)
            binding.classificationImageView.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.financialforecasting_green_700
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
        } else if (classificationString == decreaseClassificationString) {
            binding.classificationImageView.setImageResource(R.drawable.ic_trending_down)
            binding.classificationImageView.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_accent
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
        }

        //setup regression
        val regressionValue = predictionResponseDto.regressionResult
        binding.companyRegressionResultTextView.text =
            getString(R.string.sample_regression_result, regressionValue.toFloat())
        if (regressionValue > lastClosingPrice) {
            binding.regressionImageView.setImageResource(R.drawable.ic_trending_up)
            binding.regressionImageView.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.financialforecasting_green_700
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
        } else {
            binding.regressionImageView.setImageResource(R.drawable.ic_trending_down)
            binding.regressionImageView.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_accent
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun setupHistoricalPriceChart() {
        val historicalPrices =
            clientCompanyPredictionResultViewModel.predictionResponseDto.historicalClosePrice
                .reversed().toList()


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
            description.text = "Historical prices 30 days back from $predictionStartingDate"
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
        binding.companyHistoricalPriceChart.marker = CustomMarker(requireContext(), R.layout.marker_view, historicalPrices)
        binding.companyHistoricalPriceChart.invalidate()
    }

    companion object {
        const val increaseClassificationString = "Increase"
        const val decreaseClassificationString = "Decrease"
    }

}
