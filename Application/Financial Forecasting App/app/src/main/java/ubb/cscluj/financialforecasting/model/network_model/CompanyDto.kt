package ubb.cscluj.financialforecasting.model.network_model

data class CompanyDto(
    val companyId: Long = -1,
    val name: String = "",
    var stockTickerSymbol: String = "",
    var description: String = "",
    var foundedYear: Long = -1,
    var urlLink: String = "",
    var urlLogo: String = "",
    var csvDataPath: String = "",
    var readyForPrediction: Boolean = false
)