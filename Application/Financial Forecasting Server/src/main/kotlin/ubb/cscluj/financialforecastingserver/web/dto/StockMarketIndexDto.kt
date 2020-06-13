package ubb.cscluj.financialforecastingserver.web.dto

data class StockMarketIndexDto(
        val stockMarketIndexId: Long = -1,
        val name: String = "",
        var stockTickerSymbol: String = "",
        var csvDataPath: String = ""
)
