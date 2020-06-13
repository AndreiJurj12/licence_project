package ubb.cscluj.financialforecastingserver.web.dto

data class StockMarketIndexAdditionRequestDto(
        var name: String,
        var stockTickerSymbol: String,
        var csvDataPath: String
)
