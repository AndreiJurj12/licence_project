package ubb.cscluj.financialforecasting.repository.company

import ubb.cscluj.financialforecasting.database_persistence.CompanyDao
import ubb.cscluj.financialforecasting.database_persistence.FavouriteCompanyDao
import ubb.cscluj.financialforecasting.utils.logd
import ubb.cscluj.financialforecasting.model.Company
import ubb.cscluj.financialforecasting.model.network_model.CompanyAdditionRequestDto
import ubb.cscluj.financialforecasting.model.network_model.CompanyDto
import ubb.cscluj.financialforecasting.model.network_model.CompanyUpdateRequestDto
import ubb.cscluj.financialforecasting.model.network_model.UpdateAllStockDataResponseDto
import ubb.cscluj.financialforecasting.service.NetworkService
import java.net.HttpURLConnection

class CompanyRepository(
    private val networkService: NetworkService,
    private val companyDao: CompanyDao,
    private val favouriteCompanyDao: FavouriteCompanyDao
) {
    val allCompanies = companyDao.getAllCompanies()


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
        val countFeedbackMessages = companyDao.getCountCompanies()

        if (countFeedbackMessages == 0L) {
            refreshFromServer(userToken)
        }
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
}