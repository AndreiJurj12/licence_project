package ubb.cscluj.financialforecastingserver.web.dto

data class ExternalAPIMetadataDto(
        val symbol: String,
        val interval: String,
        val currency: String,
        val exchange_timezone: String,
        val exchange: String,
        val type: String
)
