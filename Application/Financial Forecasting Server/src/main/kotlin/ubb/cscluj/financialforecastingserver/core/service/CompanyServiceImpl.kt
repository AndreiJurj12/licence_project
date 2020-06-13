package ubb.cscluj.financialforecastingserver.core.service

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ubb.cscluj.financialforecastingserver.core.exceptions.IdNotFoundException
import ubb.cscluj.financialforecastingserver.core.model.Company
import ubb.cscluj.financialforecastingserver.core.model.StockMarketIndex
import ubb.cscluj.financialforecastingserver.core.model.User
import ubb.cscluj.financialforecastingserver.core.repository.CompanyRepository
import ubb.cscluj.financialforecastingserver.core.repository.FavouriteCompanyRepository
import ubb.cscluj.financialforecastingserver.core.repository.StockMarketIndexRepository
import ubb.cscluj.financialforecastingserver.core.repository.UserRepository
import ubb.cscluj.financialforecastingserver.core.validator.*
import ubb.cscluj.financialforecastingserver.web.dto.*
import java.lang.Exception
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.ArrayList

@Service
class CompanyServiceImpl @Autowired constructor(
        val companyRepository: CompanyRepository,
        val companyAdditionRequestValidator: CompanyAdditionRequestValidator,
        val companyUpdateRequestValidator: CompanyUpdateRequestValidator,
        val externalStockDataService: ExternalStockDataService,
        val userRepository: UserRepository,
        val favouriteCompanyRepository: FavouriteCompanyRepository,
        val stockMarketIndexRepository: StockMarketIndexRepository,
        val stockMarketIndexAdditionRequestValidator: StockMarketIndexAdditionRequestValidator,
        val predictionRequestValidator: PredictionRequestValidator,
        val predictionService: PredictionService,
        val historicalDataClosePriceDtoValidator: HistoricalDataClosePriceDtoValidator
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

        val allStockMarketIndexList = stockMarketIndexRepository.findAll()
        allStockMarketIndexList.forEach {
            externalStockDataService.updateCompanyLatestData(it.stockTickerSymbol, it.csvDataPath)

            logging.debug("Stock Market Index ${it.name} has stock data updated")
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

    @Transactional
    override fun saveNewStockMarketIndex(stockMarketIndexAdditionRequestDto: StockMarketIndexAdditionRequestDto): StockMarketIndex {
        stockMarketIndexAdditionRequestValidator.validate(stockMarketIndexAdditionRequestDto)


        val newStockMarketIndex = StockMarketIndex(
                name = stockMarketIndexAdditionRequestDto.name,
                stockTickerSymbol = stockMarketIndexAdditionRequestDto.stockTickerSymbol,
                csvDataPath = stockMarketIndexAdditionRequestDto.csvDataPath + "\\${stockMarketIndexAdditionRequestDto.stockTickerSymbol}.csv"
        )

        val savedStockMarketIndex = stockMarketIndexRepository.save(newStockMarketIndex)

        externalStockDataService.loadInitialData(savedStockMarketIndex.stockTickerSymbol, savedStockMarketIndex.csvDataPath)

        return savedStockMarketIndex
    }

    override fun predictionRequest(predictionRequestDto: PredictionRequestDto): PredictionResponseDto {
        predictionRequestValidator.validate(predictionRequestDto)

        val company = companyRepository.findByIdOrNull(predictionRequestDto.companyId)
                ?: throw IdNotFoundException("Id for company: ${predictionRequestDto.companyId} not found")
        if (!company.readyForPrediction) {
            throw ValidationException("Company not ready for prediction")
        }

        this.validatePredictionDateForCompany(company, predictionRequestDto.predictionStartingDay)
        //now we are ready to create the request dto for the prediction service

        return this.internalExternalPredictionRequestCall(company, predictionRequestDto.predictionStartingDay)
    }

    override fun getHistoricalDataClosePrice(historicalDataClosePriceDto: HistoricalDataClosePriceDto): List<DateClosePrice> {
        historicalDataClosePriceDtoValidator.validate(historicalDataClosePriceDto)

        val company = companyRepository.findByIdOrNull(historicalDataClosePriceDto.companyId)
                ?: throw IdNotFoundException("Id for company: ${historicalDataClosePriceDto.companyId} not found")

        val allHistoricalPrices = this.getAllCloseDataPricesOfCompany(company)

        return allHistoricalPrices.sortedByDescending { it.date }
                .take(historicalDataClosePriceDto.requiredCount.toInt())
                .toList()
    }

    private fun internalExternalPredictionRequestCall(company: Company, startingPredictionDay: String): PredictionResponseDto {
        val dowJonesIndustrialStockMarketIndex =
                stockMarketIndexRepository.findStockMarketIndexByStockTickerSymbol(dowJonesIndustrialStockTickerSymbol)
        val nasdaqCompositeStockMarketIndex =
                stockMarketIndexRepository.findStockMarketIndexByStockTickerSymbol(nasdaqCompositeStockTickerSymbol)
        val realStartingPredictionDay = this.getClosestWorkingDayIfNotWorking(company, startingPredictionDay)

        val externalPredictionRequest = ExternalPredictionRequest(
                companyTickerSymbol = company.stockTickerSymbol,
                companyCsvDataPath = company.csvDataPath,
                predictionStartingDay = realStartingPredictionDay,
                dowJonesIndustrialCsvDataPath = dowJonesIndustrialStockMarketIndex!!.csvDataPath,
                nasdaqCompositeCsvDataPath = nasdaqCompositeStockMarketIndex!!.csvDataPath
        )

        val externalPredictionResponse = predictionService.predict(externalPredictionRequest)


        //get historical prices also for the request
        val historicalPrices = this.getHistoricalClosePriceBetweenDates(startingPredictionDay, company)

        //get expected prediction price if available
        val expectedPredictedPrice = this.getFutureClosePredictionIfExists(startingPredictionDay, company)

        return PredictionResponseDto(
                message = "Successful prediction",
                classificationResult = externalPredictionResponse.classificationResult,
                regressionResult = externalPredictionResponse.regressionResult,
                historicalClosePrice = historicalPrices,
                expectedPredictedPrice = expectedPredictedPrice
        )
    }

    private fun getHistoricalClosePriceBetweenDates(startingPredictionDay: String, company: Company): List<DateClosePrice> {
        val allHistoricalPrices = this.getAllCloseDataPricesOfCompany(company)

        return allHistoricalPrices
                .filter { it.date <= LocalDate.parse(startingPredictionDay, dateFormatter) }
                .sortedByDescending { it.date }
                .take(noDaysResponseWithPrediction)
                .toList()
    }

    private fun getFutureClosePredictionIfExists(startingPredictionDay: String, company: Company): DateClosePrice? {
        val allHistoricalPrices = this.getAllCloseDataPricesOfCompany(company)

        return allHistoricalPrices
                .filter { it.date > LocalDate.parse(startingPredictionDay, dateFormatter) }
                .sortedBy { it.date }
                .getOrNull(noDaysFuturePrediction - 1)
    }

    private fun getAllCloseDataPricesOfCompany(company: Company): List<DateClosePrice> {
        val allHistoricalPrices: MutableList<DateClosePrice> = mutableListOf()

        csvReader().open(company.csvDataPath) {
            readAllWithHeaderAsSequence().forEach { row ->
                val currentDateString = row[dateHeaderLabel]
                if (currentDateString != null) {
                    val currentDate = LocalDate.parse(currentDateString, dateFormatter)
                    val currentClosePrice = row[closePriceHeaderLabel]?.toDouble() ?: 0.0
                    allHistoricalPrices.add(DateClosePrice(
                            date = currentDate,
                            closePrice = currentClosePrice
                    ))
                }
            }
        }

        return allHistoricalPrices.toList()
    }

    private fun getClosestWorkingDayIfNotWorking(company: Company, startingPredictionDay: String): String {
        val convertedStartingPredictionDate = LocalDate.parse(startingPredictionDay, dateFormatter)
        var closestDateAvailable = LocalDate.of(1950, 1, 1)

        csvReader().open(company.csvDataPath) {
            readAllWithHeaderAsSequence().forEach { row ->
                val currentDateString = row[dateHeaderLabel]
                if (currentDateString != null) {
                    val currentDate = LocalDate.parse(currentDateString, dateFormatter)
                    if (currentDate <= convertedStartingPredictionDate && closestDateAvailable < currentDate) {
                        closestDateAvailable = currentDate
                    }
                }
            }
        }

        return closestDateAvailable.format(dateFormatter)
    }

    private fun validatePredictionDateForCompany(company: Company, startingPredictionDay: String) {
        val companyExtremeDates = this.getMinimumAndMaximumDateFromCompanyData(company)
        val companyMinimumDate = companyExtremeDates.first
        val companyMaximumDate = companyExtremeDates.second

        val requestedPredictionDate = LocalDate.parse(startingPredictionDay, dateFormatter)

        if (requestedPredictionDate > companyMaximumDate) {
            throw ValidationException("Prediction date is too much into the future")
        }
        if (requestedPredictionDate < companyMinimumDate) {
            throw ValidationException("Prediction date is too much into the past")
        }

        val noDaysAvailableForPreprocessing = Duration.between(companyMinimumDate.atStartOfDay(), requestedPredictionDate.atStartOfDay()).toDays()
        if (noDaysAvailableForPreprocessing < noDaysRequiredForPrediction) {
            throw ValidationException("Prediction date is too much into the past - not available data for preprocessing")
        }
    }

    private fun getMinimumAndMaximumDateFromCompanyData(company: Company): Pair<LocalDate, LocalDate> {
        var minimumDate = LocalDate.of(2050, 1, 1)
        var maximumDate = LocalDate.of(1950, 1, 1)

        csvReader().open(company.csvDataPath) {
            readAllWithHeaderAsSequence().forEach { row ->
                val currentDateString = row[dateHeaderLabel]
                if (currentDateString != null) {
                    val currentDate = LocalDate.parse(currentDateString, dateFormatter)

                    if (minimumDate > currentDate)
                        minimumDate = currentDate

                    if (maximumDate < currentDate)
                        maximumDate = currentDate
                }
            }
        }

        return Pair(minimumDate, maximumDate)
    }

    companion object CustomFields {
        private const val dowJonesIndustrialStockTickerSymbol = "DJI"
        private const val nasdaqCompositeStockTickerSymbol = "IXIC"


        private const val dateFormat = "yyyy-MM-dd"
        private var dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat)

        const val dateHeaderLabel: String = "Date"
        const val closePriceHeaderLabel: String = "Close"
        var headersList: List<String> = listOf("Date", "Open", "High", "Low", "Close", "Volume")
        val dateTimeStrToLocalDate: (String) -> LocalDate = {
            LocalDate.parse(it, dateFormatter)
        }

        const val noDaysRequiredForPrediction = 252 * 2
        const val noDaysResponseWithPrediction = 30
        const val noDaysFuturePrediction = 5
    }
}
