package ubb.cscluj.financialforecastingserver.web.dto

data class CompanyAdditionRequestDto(
        var name: String,
        var stockTickerSymbol: String,
        var description: String,
        var foundedYear: Long,
        var urlLink: String,
        var urlLogo: String,
        var csvDataPath: String
)
