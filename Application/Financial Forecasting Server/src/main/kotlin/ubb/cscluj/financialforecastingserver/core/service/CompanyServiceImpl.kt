package ubb.cscluj.financialforecastingserver.core.service

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ubb.cscluj.financialforecastingserver.core.exceptions.IdNotFoundException
import ubb.cscluj.financialforecastingserver.core.model.Company
import ubb.cscluj.financialforecastingserver.core.model.User
import ubb.cscluj.financialforecastingserver.core.repository.CompanyRepository
import ubb.cscluj.financialforecastingserver.core.repository.FavouriteCompanyRepository
import ubb.cscluj.financialforecastingserver.core.repository.UserRepository
import ubb.cscluj.financialforecastingserver.core.validator.CompanyAdditionRequestValidator
import ubb.cscluj.financialforecastingserver.core.validator.CompanyUpdateRequestValidator
import ubb.cscluj.financialforecastingserver.core.validator.ValidationException
import ubb.cscluj.financialforecastingserver.web.dto.CompanyAdditionRequestDto
import ubb.cscluj.financialforecastingserver.web.dto.CompanyUpdateRequestDto
import ubb.cscluj.financialforecastingserver.web.dto.FavouriteCompanyAdditionRequestDto
import ubb.cscluj.financialforecastingserver.web.dto.FavouriteCompanyRemovalRequestDto
import java.lang.Exception

@Service
class CompanyServiceImpl @Autowired constructor(
        val companyRepository: CompanyRepository,
        val companyAdditionRequestValidator: CompanyAdditionRequestValidator,
        val companyUpdateRequestValidator: CompanyUpdateRequestValidator,
        val externalStockDataService: ExternalStockDataService,
        val userRepository: UserRepository,
        val favouriteCompanyRepository: FavouriteCompanyRepository
) : CompanyService {
    private var logging: Logger = LogManager.getLogger(CompanyServiceImpl::class.java)



    @Transactional
    override fun saveNewCompany(companyAdditionRequestDto: CompanyAdditionRequestDto): Company {
        companyAdditionRequestValidator.validate(companyAdditionRequestDto)

        val newCompany = Company(
                name = companyAdditionRequestDto.name,
                stockTickerSymbol = companyAdditionRequestDto.stockTickerSymbol,
                description = companyAdditionRequestDto.description,
                foundedYear = companyAdditionRequestDto.foundedYear,
                urlLink = companyAdditionRequestDto.urlLink,
                urlLogo = companyAdditionRequestDto.urlLogo,
                csvDataPath = companyAdditionRequestDto.csvDataPath + "\\${companyAdditionRequestDto.stockTickerSymbol}.csv"
        )
        val savedCompany = companyRepository.save(newCompany)

        externalStockDataService.loadInitialData(savedCompany.stockTickerSymbol, savedCompany.csvDataPath)

        return savedCompany
    }

    @Transactional
    override fun updateCompanyDetails(companyUpdateRequestDto: CompanyUpdateRequestDto): Company {
        companyUpdateRequestValidator.validate(companyUpdateRequestDto)

        val companyManagedEntity: Company = companyRepository.findByIdOrNull(companyUpdateRequestDto.companyId)
                ?: throw IdNotFoundException("Id for company: ${companyUpdateRequestDto.companyId} not found")

        companyManagedEntity.apply {
            name = companyUpdateRequestDto.name
            stockTickerSymbol = companyUpdateRequestDto.stockTickerSymbol
            description = companyUpdateRequestDto.description
            foundedYear = companyUpdateRequestDto.foundedYear
            urlLink = companyUpdateRequestDto.urlLink
            urlLogo = companyUpdateRequestDto.urlLogo
            csvDataPath = companyUpdateRequestDto.csvDataPath
            readyForPrediction = companyUpdateRequestDto.readyForPrediction
        }

        return companyManagedEntity
    }

    override fun updateAllStockData() {
        val allCompaniesList = companyRepository.findAll()
        allCompaniesList.forEach {
            externalStockDataService.updateCompanyLatestData(it.stockTickerSymbol, it.csvDataPath)

            logging.debug("Company ${it.name} has stock data updated")
        }
    }

    override fun getFavouriteUserCompanies(userId: Long): List<Company> {
        val userWithFavouriteCompaniesLoaded: User = userRepository.getUserByIdWithFavouritesCompaniesSetLoaded(userId)
                ?: throw IdNotFoundException("Id for user: $userId not found")

        return userWithFavouriteCompaniesLoaded.favouriteCompaniesSet.map { it.company }.toList()
    }

    override fun getAllCompanies(): List<Company> = companyRepository.findAll()

    @Transactional
    override fun addNewFavouriteUserCompany(favouriteCompanyAdditionRequestDto: FavouriteCompanyAdditionRequestDto, userId: Long) {
        val user: User = userRepository.findByIdOrNull(userId)
                ?: throw IdNotFoundException("Id for user: $userId not found")

        val company: Company = companyRepository.findByIdOrNull(favouriteCompanyAdditionRequestDto.companyId)
                ?: throw IdNotFoundException("Id for company: ${favouriteCompanyAdditionRequestDto.companyId} not found")

        user.addNewFavouriteCompany(company)
    }

    @Transactional
    override fun removeFavouriteUserCompany(favouriteCompanyRemovalRequestDto: FavouriteCompanyRemovalRequestDto, userId: Long) {
        val user: User = userRepository.findByIdOrNull(userId)
                ?: throw IdNotFoundException("Id for user: $userId not found")

        val company: Company = companyRepository.findByIdOrNull(favouriteCompanyRemovalRequestDto.companyId)
                ?: throw IdNotFoundException("Id for company: ${favouriteCompanyRemovalRequestDto.companyId} not found")

        val favouriteCompany = favouriteCompanyRepository.getFavouriteCompanyByUserAndCompany(user, company)
        if (favouriteCompany != null) {
            favouriteCompanyRepository.delete(favouriteCompany)
        }
    }
}
