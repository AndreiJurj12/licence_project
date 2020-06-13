package ubb.cscluj.financialforecastingserver.core.service

import ubb.cscluj.financialforecastingserver.core.exceptions.ExternalAPIFailedException
import ubb.cscluj.financialforecastingserver.core.exceptions.IdNotFoundException
import ubb.cscluj.financialforecastingserver.core.model.Company
import ubb.cscluj.financialforecastingserver.core.model.StockMarketIndex
import ubb.cscluj.financialforecastingserver.core.validator.ValidationException
import ubb.cscluj.financialforecastingserver.web.dto.*

interface CompanyService {
    @Throws(ValidationException::class, ExternalAPIFailedException::class)
    fun saveNewCompany(companyAdditionRequestDto: CompanyAdditionRequestDto): Company

    @Throws(ValidationException::class, IdNotFoundException::class)
    fun updateCompanyDetails(companyUpdateRequestDto: CompanyUpdateRequestDto): Company

    @Throws(ExternalAPIFailedException::class)
    fun updateAllStockData()

    @Throws(IdNotFoundException::class)
    fun getFavouriteUserCompanies(userId: Long): List<Company>

    fun getAllCompanies(): List<Company>

    @Throws(IdNotFoundException::class)
    fun addNewFavouriteUserCompany(favouriteCompanyAdditionRequestDto: FavouriteCompanyAdditionRequestDto, userId: Long)

    @Throws(IdNotFoundException::class)
    fun removeFavouriteUserCompany(favouriteCompanyRemovalRequestDto: FavouriteCompanyRemovalRequestDto, userId: Long)


    @Throws(ValidationException::class, ExternalAPIFailedException::class)
    fun saveNewStockMarketIndex(stockMarketIndexAdditionRequestDto: StockMarketIndexAdditionRequestDto): StockMarketIndex


    @Throws(ValidationException::class)
    fun predictionRequest(predictionRequestDto: PredictionRequestDto): PredictionResponseDto


    @Throws(ValidationException::class)
    fun getHistoricalDataClosePrice(historicalDataClosePriceDto: HistoricalDataClosePriceDto): List<DateClosePrice>
}
