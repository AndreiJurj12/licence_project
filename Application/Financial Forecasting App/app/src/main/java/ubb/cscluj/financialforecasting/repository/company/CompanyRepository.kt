package ubb.cscluj.financialforecasting.repository.company

import com.google.gson.Gson
import retrofit2.Response
import ubb.cscluj.financialforecasting.database_persistence.CompanyDao
import ubb.cscluj.financialforecasting.database_persistence.FavouriteCompanyDao
import ubb.cscluj.financialforecasting.utils.logd
import ubb.cscluj.financialforecasting.model.Company
import ubb.cscluj.financialforecasting.model.FavouriteCompany
import ubb.cscluj.financialforecasting.model.network_model.*
import ubb.cscluj.financialforecasting.service.NetworkService
import ubb.cscluj.financialforecasting.utils.toFavouriteCompany
import java.net.HttpURLConnection

class CompanyRepository(
    private val networkService: NetworkService,
    private val companyDao: CompanyDao,
    private val favouriteCompanyDao: FavouriteCompanyDao
) {
    val allCompanies = companyDao.getAllCompanies()
    val allFavouriteCompanies = favouriteCompanyDao.getAllFavouriteCompanies()


    suspend fun refreshFromServer(userToken: String) {
        logd("Enter CompanyRepository: refreshFromServer($userToken)")
        val response = networkService.service.getAllCompanies(userToken)
        logd("Http response obtained: $response")
        if (response.raw().code != HttpURLConnection.HTTP_OK) {
            throw CompanyNetworkException(
                "Refresh from server failed!"
            )
        }

        val newCompanies = response.body() as List<CompanyDto>
        val convertedCompanies = newCompanies.map {
            Company(
                id = it.companyId,
                name = it.name,
                stockTickerSymbol = it.stockTickerSymbol,
                description = it.description,
                foundedYear = it.foundedYear,
                urlLink = it.urlLink,
                urlLogo = it.urlLogo,
                csvDataPath = it.csvDataPath,
                readyForPrediction = it.readyForPrediction
            )
        }
        logd("NewCompanies obtained: $convertedCompanies")

        companyDao.clearDatabaseTable()
        companyDao.insertCompanyList(convertedCompanies)
    }

    suspend fun initialLoading(userToken: String) {
        val countCompanies = companyDao.getCountCompanies()

        if (countCompanies == 0L) {
            refreshFromServer(userToken)
        }
    }

    suspend fun refreshFromServerFavourites(userToken: String) {
        logd("Enter CompanyRepository: refreshFromServerFavourites($userToken)")
        val response = networkService.service.getAllFavouriteCompanies(userToken)
        logd("Http response obtained: $response")
        if (response.raw().code != HttpURLConnection.HTTP_OK) {
            throw CompanyNetworkException(
                "Refresh from server favourites failed!"
            )
        }

        val newCompanies = response.body() as List<CompanyDto>
        val convertedCompanies = newCompanies.map {
            FavouriteCompany(
                id = it.companyId,
                name = it.name,
                stockTickerSymbol = it.stockTickerSymbol,
                description = it.description,
                foundedYear = it.foundedYear,
                urlLink = it.urlLink,
                urlLogo = it.urlLogo,
                csvDataPath = it.csvDataPath,
                readyForPrediction = it.readyForPrediction
            )
        }
        logd("NewFavouritesCompanies obtained: $convertedCompanies")

        favouriteCompanyDao.clearDatabaseTable()
        favouriteCompanyDao.insertFavouriteCompanyList(convertedCompanies)
    }

    suspend fun initialLoadingFavourite(userToken: String) {
        /*
        val countCompanies = favouriteCompanyDao.getCountFavouriteCompanies()

        if (countCompanies == 0L) {
            refreshFromServerFavourites(userToken)
        }
         */
        //for favourites we must always load since we don't differentiate based on userId
        refreshFromServerFavourites(userToken)
    }

    suspend fun addNewCompany(company: Company, userToken: String) {
        logd("Enter CompanyRepository: addNewCompany($company, $userToken)")
        val companyAdditionRequestDto = CompanyAdditionRequestDto(
            name = company.name,
            stockTickerSymbol = company.stockTickerSymbol,
            description = company.description,
            foundedYear = company.foundedYear,
            urlLink = company.urlLink,
            urlLogo = company.urlLogo,
            csvDataPath = company.csvDataPath
        )

        val response = networkService.service.saveNewCompany(userToken, companyAdditionRequestDto)
        logd("Http response obtained: $response")
        if (response.raw().code != HttpURLConnection.HTTP_OK) {
            throw CompanyNetworkException(
                "Save new company from server failed!"
            )
        }

        val savedCompanyDto = response.body() as CompanyDto
        val savedCompany = Company(
            id = savedCompanyDto.companyId,
            name = savedCompanyDto.name,
            stockTickerSymbol = savedCompanyDto.stockTickerSymbol,
            description = savedCompanyDto.description,
            foundedYear = savedCompanyDto.foundedYear,
            urlLink = savedCompanyDto.urlLink,
            urlLogo = savedCompanyDto.urlLogo,
            csvDataPath = savedCompanyDto.csvDataPath,
            readyForPrediction = savedCompanyDto.readyForPrediction
        )

        companyDao.insertCompany(savedCompany)
    }

    suspend fun findCompanyById(companyId: Long): Company {
        return companyDao.findCompanyById(companyId)
    }

    suspend fun updateCompany(company: Company, userToken: String) {
        logd("Enter CompanyRepository: updateCompany($company, $userToken)")
        val companyUpdateRequestDto = CompanyUpdateRequestDto(
            companyId = company.id,
            name = company.name,
            stockTickerSymbol = company.stockTickerSymbol,
            description = company.description,
            foundedYear = company.foundedYear,
            urlLink = company.urlLink,
            urlLogo = company.urlLogo,
            csvDataPath = company.csvDataPath,
            readyForPrediction = company.readyForPrediction
        )

        val response = networkService.service.updateCompany(userToken, companyUpdateRequestDto)
        logd("Http response obtained: $response")
        if (response.raw().code != HttpURLConnection.HTTP_OK) {
            throw CompanyNetworkException(
                "Update company from server failed!"
            )
        }

        val updatedCompanyDto = response.body() as CompanyDto
        val updatedCompany = Company(
            id = updatedCompanyDto.companyId,
            name = updatedCompanyDto.name,
            stockTickerSymbol = updatedCompanyDto.stockTickerSymbol,
            description = updatedCompanyDto.description,
            foundedYear = updatedCompanyDto.foundedYear,
            urlLink = updatedCompanyDto.urlLink,
            urlLogo = updatedCompanyDto.urlLogo,
            csvDataPath = updatedCompanyDto.csvDataPath,
            readyForPrediction = updatedCompanyDto.readyForPrediction
        )

        companyDao.updateCompany(updatedCompany)
    }

    suspend fun updateAllStockData(userToken: String) {
        logd("Enter CompanyRepository: updateAllStockData($userToken)")
        val response = networkService.service.updateAllStockData(userToken)
        logd("Http response obtained: $response")
        if (response.raw().code != HttpURLConnection.HTTP_OK) {
            val updateAllStockDataResponseDto = response.body() as UpdateAllStockDataResponseDto
            throw CompanyNetworkException(updateAllStockDataResponseDto.message)
        }
    }

    suspend fun switchFavouriteCompany(company: Company, userToken: String) {
        if (favouriteCompanyDao.findFavouriteCompanyById(company.id) == null) {
            addNewFavouriteCompany(company, userToken)
        } else {
            removeFavouriteCompany(company, userToken)
        }
    }

    private suspend fun addNewFavouriteCompany(company: Company, userToken: String) {
        val response = networkService.service.addNewFavouriteCompany(
            userToken,
            FavouriteCompanyAdditionRequestDto(companyId = company.id)
        )
        logd("Http response obtained: $response")
        if (response.raw().code != HttpURLConnection.HTTP_OK) {
            val favouriteCompanyAdditionResponseDto =
                response.body() as FavouriteCompanyAdditionResponseDto
            throw CompanyNetworkException(favouriteCompanyAdditionResponseDto.message)
        }

        favouriteCompanyDao.insertFavouriteCompany(company.toFavouriteCompany())
    }

    private suspend fun removeFavouriteCompany(company: Company, userToken: String) {
        val response = networkService.service.removeFavouriteCompany(
            userToken,
            FavouriteCompanyRemovalRequestDto(companyId = company.id)
        )
        logd("Http response obtained: $response")
        if (response.raw().code != HttpURLConnection.HTTP_OK) {
            val favouriteCompanyRemovalResponseDto =
                response.body() as FavouriteCompanyRemovalResponseDto
            throw CompanyNetworkException(favouriteCompanyRemovalResponseDto.message)
        }

        favouriteCompanyDao.deleteFavouriteCompany(company.id)
    }


    suspend fun requirePrediction(companyId: Long, predictionStartingDate: String, userToken: String) : PredictionResponseDto{
        val response = networkService.service.requirePrediction(
            userToken,
            PredictionRequestDto(
                companyId = companyId,
                predictionStartingDay = predictionStartingDate
            )
        )
        logd("Http response obtained: $response")
        if (response.raw().code != HttpURLConnection.HTTP_OK) {
            val predictionResponseDto =
                Gson().fromJson(response.errorBody()!!.charStream(), PredictionResponseDto::class.java)
            throw CompanyNetworkException(predictionResponseDto.message)
        }

        return response.body() as PredictionResponseDto
    }

    suspend fun getHistoricalDataClosePrice(companyId: Long, requireNoDays: Long, userToken: String): List<DateClosePrice> {
        val response = networkService.service.getHistoricalDataClosePrice(
            userToken = userToken,
            historicalDataClosePriceDto = HistoricalDataClosePriceDto(
                companyId = companyId,
                requiredCount = requireNoDays
            )
        )
        logd("Http response obtained: $response")
        if (response.raw().code != HttpURLConnection.HTTP_OK) {
            throw CompanyNetworkException("Failed obtaining historical prices on the server!")
        }

        return response.body() as List<DateClosePrice>
    }
}