package ubb.cscluj.financialforecastingserver.web.dto

data class ExternalPredictionRequest(
        val companyTickerSymbol: String,
        val companyCsvDataPath: String,
        val predictionStartingDay: String,
        val dowJonesIndustrialCsvDataPath: String,
        val nasdaqCompositeCsvDataPath: String
)
