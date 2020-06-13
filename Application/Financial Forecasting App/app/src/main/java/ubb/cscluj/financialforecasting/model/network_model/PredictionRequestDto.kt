package ubb.cscluj.financialforecasting.model.network_model

data class PredictionRequestDto(
        val companyId: Long,
        val predictionStartingDay: String
)
