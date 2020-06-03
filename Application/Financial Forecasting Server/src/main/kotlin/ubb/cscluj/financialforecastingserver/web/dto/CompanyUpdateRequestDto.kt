package ubb.cscluj.financialforecastingserver.web.dto

data class CompanyUpdateRequestDto(
        var companyId: Long = -1,
        var name: String = "",
        var stockTickerSymbol: String = "",
        var description: String = "",
        var foundedYear: Long = -1,
        var urlLink: String = "",
        var urlLogo: String = "",
        var csvDataPath: String = "",
        var readyForPrediction: Boolean = false
)
