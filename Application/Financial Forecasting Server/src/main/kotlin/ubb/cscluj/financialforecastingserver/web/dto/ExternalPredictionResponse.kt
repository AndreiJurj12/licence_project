package ubb.cscluj.financialforecastingserver.web.dto

data class ExternalPredictionResponse(
        val classificationResult: String,
        val regressionResult: Double
)
