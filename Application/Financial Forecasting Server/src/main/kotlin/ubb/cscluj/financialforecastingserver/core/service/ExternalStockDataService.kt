package ubb.cscluj.financialforecastingserver.core.service

import ubb.cscluj.financialforecastingserver.core.exceptions.ExternalAPIFailedException

interface ExternalStockDataService {

    @Throws(ExternalAPIFailedException::class)
    fun loadInitialData(stockTickerSymbol: String, csvDataPath: String)

    @Throws(ExternalAPIFailedException::class)
    fun updateCompanyLatestData(stockTickerSymbol: String, csvDataPath: String)
}
