package ubb.cscluj.financialforecastingserver.web.dto

data class PredictionRequestDto(
        val companyId: Long,
        val predictionStartingDay: String
)
