package ubb.cscluj.financialforecasting.model.network_model

data class CompanyAdditionRequestDto(
        var name: String,
        var stockTickerSymbol: String,
        var description: String,
        var foundedYear: Long,
        var urlLink: String,
        var urlLogo: String,
        var csvDataPath: String
)
