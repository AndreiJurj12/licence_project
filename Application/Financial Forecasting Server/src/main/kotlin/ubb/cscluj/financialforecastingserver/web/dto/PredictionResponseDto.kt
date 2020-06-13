package ubb.cscluj.financialforecastingserver.web.dto

data class PredictionResponseDto(
        val message: String = "",
        val classificationResult: String = "invalid",
        val regressionResult: Double = -1.0,
        val historicalClosePrice: List<DateClosePrice> = emptyList(),
        val expectedPredictedPrice: DateClosePrice? = null
)
