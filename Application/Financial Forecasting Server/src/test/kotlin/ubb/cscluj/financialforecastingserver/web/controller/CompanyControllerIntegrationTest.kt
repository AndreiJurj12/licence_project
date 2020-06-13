package ubb.cscluj.financialforecastingserver.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.PropertySource
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import ubb.cscluj.financialforecastingserver.core.model.*
import ubb.cscluj.financialforecastingserver.core.repository.CompanyRepository
import ubb.cscluj.financialforecastingserver.core.repository.FavouriteCompanyRepository
import ubb.cscluj.financialforecastingserver.core.repository.StockMarketIndexRepository
import ubb.cscluj.financialforecastingserver.core.repository.UserRepository
import ubb.cscluj.financialforecastingserver.web.dto.*
import ubb.cscluj.financialforecastingserver.web.mapper.CompanyDtoMapper
import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource("classpath:junit-platform.properties")
class CompanyControllerIntegrationTest(
        @Autowired val restTemplate: TestRestTemplate
) {
    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var companyRepository: CompanyRepository

    @Autowired
    lateinit var favouriteCompanyRepository: FavouriteCompanyRepository

    @Autowired
    lateinit var stockMarketIndexRepository: StockMarketIndexRepository

    @Autowired
    lateinit var companyDtoMapper: CompanyDtoMapper

    private val authorizationHeader = "Authorization"

    private val objectMapper = ObjectMapper()
    lateinit var headers: HttpHeaders
    lateinit var initialUser: User
    lateinit var initialAdmin: User
    lateinit var initialCompany: Company
    lateinit var initialFavouriteCompany: FavouriteCompany
    lateinit var initialStockMarketIndex: StockMarketIndex

    @BeforeAll
    fun setup() {
        headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set(authorizationHeader, "none")

        initialUser = User(email = "licenta@gmail.com", password = "12345678", isAdmin = false)
        initialUser = userRepository.saveAndFlush(initialUser)


        initialAdmin = User(email = "admin@gmail.com", password = "12345678", isAdmin = true)
        initialAdmin = userRepository.saveAndFlush(initialAdmin)
    }

    @BeforeEach
    fun setupMessages() {
        initialCompany = Company(
                name = "Company",
                stockTickerSymbol = "Invalid_Symbol",
                description = "Description",
                foundedYear = 2000L,
                urlLink = "www.link.com",
                urlLogo = "www.logo.com",
                csvDataPath = "invalid_path",
                readyForPrediction = true
        )
        initialCompany = companyRepository.save(initialCompany)

        initialFavouriteCompany = FavouriteCompany()
        initialFavouriteCompany.user = initialUser
        initialFavouriteCompany.company = initialCompany
        initialFavouriteCompany = favouriteCompanyRepository.save(initialFavouriteCompany)

        initialStockMarketIndex = StockMarketIndex(
                name = "Stock Index",
                stockTickerSymbol = "Invalid_Symbol",
                csvDataPath = "Invalid csv data path"
        )
        initialStockMarketIndex = stockMarketIndexRepository.save(initialStockMarketIndex)
    }

    @AfterEach
    fun teardownEach() {
        favouriteCompanyRepository.deleteAll()
        companyRepository.deleteAll()
        stockMarketIndexRepository.deleteAll()
    }

    @Test
    fun `Unsuccessful saveNewCompany Call`() {
        val validLoginRequestDto = LoginRequestDto(
                email = "admin@gmail.com",
                password = "12345678"
        )
        val request: HttpEntity<LoginRequestDto> = HttpEntity(validLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)
        val userToken = response.body!!.userToken
        headers.set(authorizationHeader, userToken)

        val companyAdditionRequestDto = CompanyAdditionRequestDto(
                name = "Sample name",
                stockTickerSymbol = "Some symbol",
                description = "Some description",
                foundedYear = 2000L,
                urlLink = "www.link.com",
                urlLogo = "www.logo.com",
                csvDataPath = "" //invalid csv data path
        )

        val requestSave: HttpEntity<CompanyAdditionRequestDto> = HttpEntity(companyAdditionRequestDto, headers)
        val responseSave: ResponseEntity<CompanyDto> = restTemplate.postForEntity("/api/company/admin/save_new_company", requestSave, CompanyDto::class.java)

        assertThat(responseSave.statusCode, equalTo(HttpStatus.INTERNAL_SERVER_ERROR))
        assertThat(responseSave.body!!.companyId, equalTo(-1L))
    }

    @Test
    fun `Unsuccessful updateCompanyDetails Call`() {
        val validLoginRequestDto = LoginRequestDto(
                email = "admin@gmail.com",
                password = "12345678"
        )
        val request: HttpEntity<LoginRequestDto> = HttpEntity(validLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)
        val userToken = response.body!!.userToken
        headers.set(authorizationHeader, userToken)

        val companyUpdateRequestDto = CompanyUpdateRequestDto(
                companyId = 1L,
                name = "Sample name",
                stockTickerSymbol = "Some symbol",
                description = "Some description",
                foundedYear = 2000L,
                urlLink = "www.link.com",
                urlLogo = "www.logo.com",
                csvDataPath = "", //invalid csv data path
                readyForPrediction = true
        )

        val requestUpdate: HttpEntity<CompanyUpdateRequestDto> = HttpEntity(companyUpdateRequestDto, headers)
        val responseUpdate: ResponseEntity<CompanyDto> = restTemplate.exchange("/api/company/admin/update_company_details", HttpMethod.PUT, requestUpdate, CompanyDto::class.java)

        assertThat(responseUpdate.statusCode, equalTo(HttpStatus.INTERNAL_SERVER_ERROR))
        assertThat(responseUpdate.body!!.companyId, equalTo(-1L))
    }

    @Test
    fun `Successful updateCompanyDetails Call`() {
        val validLoginRequestDto = LoginRequestDto(
                email = "admin@gmail.com",
                password = "12345678"
        )
        val request: HttpEntity<LoginRequestDto> = HttpEntity(validLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)
        val userToken = response.body!!.userToken
        headers.set(authorizationHeader, userToken)

        val companyUpdateRequestDto = CompanyUpdateRequestDto(
                companyId = initialCompany.id,
                name = "Sample name",
                stockTickerSymbol = "Some symbol",
                description = "Some description",
                foundedYear = 2001L,
                urlLink = "www.link.com",
                urlLogo = "www.logo.com",
                csvDataPath = "valid_path", //invalid csv data path
                readyForPrediction = true
        )

        val requestUpdate: HttpEntity<CompanyUpdateRequestDto> = HttpEntity(companyUpdateRequestDto, headers)
        val responseUpdate: ResponseEntity<CompanyDto> = restTemplate.exchange("/api/company/admin/update_company_details", HttpMethod.PUT, requestUpdate, CompanyDto::class.java)

        assertThat(responseUpdate.statusCode, equalTo(HttpStatus.OK))
        assertThat(responseUpdate.body!!.companyId, equalTo(initialCompany.id))
        assertThat(responseUpdate.body!!.name, equalTo(companyUpdateRequestDto.name))
        assertThat(responseUpdate.body!!.stockTickerSymbol, equalTo(companyUpdateRequestDto.stockTickerSymbol))
        assertThat(responseUpdate.body!!.description, equalTo(companyUpdateRequestDto.description))
        assertThat(responseUpdate.body!!.foundedYear, equalTo(companyUpdateRequestDto.foundedYear))
        assertThat(responseUpdate.body!!.urlLink, equalTo(companyUpdateRequestDto.urlLink))
        assertThat(responseUpdate.body!!.urlLogo, equalTo(companyUpdateRequestDto.urlLogo))
        assertThat(responseUpdate.body!!.csvDataPath, equalTo(companyUpdateRequestDto.csvDataPath))
        assertThat(responseUpdate.body!!.readyForPrediction, equalTo(companyUpdateRequestDto.readyForPrediction))
    }


    @Test
    fun `Unsuccessful updateAllStockData Call - fails because invalid stock symbol name`() {
        val validLoginRequestDto = LoginRequestDto(
                email = "admin@gmail.com",
                password = "12345678"
        )
        val request: HttpEntity<LoginRequestDto> = HttpEntity(validLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)
        val userToken = response.body!!.userToken
        headers.set(authorizationHeader, userToken)


        val requestUpdate: HttpEntity<Any> = HttpEntity(headers)
        val responseUpdate: ResponseEntity<UpdateAllStockDataResponseDto> = restTemplate.postForEntity("/api/company/admin/update_all_stock_data", requestUpdate, UpdateAllStockDataResponseDto::class.java)

        assertThat(responseUpdate.statusCode, equalTo(HttpStatus.INTERNAL_SERVER_ERROR))
        assertThat(responseUpdate.body!!.message.contains("Updating all stock data failed with error messag"), equalTo(true))
    }

    @Test
    fun `Successful updateAllStockData Call when empty db`() {
        //first we clean all repository important
        favouriteCompanyRepository.deleteAll()
        companyRepository.deleteAll()
        stockMarketIndexRepository.deleteAll()

        val validLoginRequestDto = LoginRequestDto(
                email = "admin@gmail.com",
                password = "12345678"
        )
        val request: HttpEntity<LoginRequestDto> = HttpEntity(validLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)
        val userToken = response.body!!.userToken
        headers.set(authorizationHeader, userToken)


        val requestUpdate: HttpEntity<Any> = HttpEntity(headers)
        val responseUpdate: ResponseEntity<UpdateAllStockDataResponseDto> = restTemplate.postForEntity("/api/company/admin/update_all_stock_data", requestUpdate, UpdateAllStockDataResponseDto::class.java)
        assertThat(responseUpdate.statusCode, equalTo(HttpStatus.OK))
        assertThat(responseUpdate.body!!.message.contains("Successful update for all stock data companies"), equalTo(true))
    }

    @Test
    fun `Successful getFavouriteUserCompanies Call`() {
        var newCompany = Company(
                name = "new",
                stockTickerSymbol = "symbol",
                description = "something",
                foundedYear = 1999L,
                urlLink = "www.link.com",
                urlLogo = "www.logo.com",
                csvDataPath = "something",
                readyForPrediction = true
        )
        newCompany = companyRepository.saveAndFlush(newCompany)

        val validLoginRequestDto = LoginRequestDto(
                email = "licenta@gmail.com",
                password = "12345678"
        )
        val request: HttpEntity<LoginRequestDto> = HttpEntity(validLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)
        val userToken = response.body!!.userToken
        headers.set(authorizationHeader, userToken)

        val requestGetAll: HttpEntity<Any> = HttpEntity(headers)
        val responseGetAll: ResponseEntity<List<CompanyDto>> =
                restTemplate.exchange("/api/company/get_favourite_user_companies", HttpMethod.GET, requestGetAll, object : ParameterizedTypeReference<List<CompanyDto>>() {})

        assertThat(responseGetAll.statusCode, equalTo(HttpStatus.OK))
        assertThat(responseGetAll.body!!.contains(companyDtoMapper.convertModelToDto(initialCompany)), equalTo(true))
        assertThat(responseGetAll.body!!.contains(companyDtoMapper.convertModelToDto(newCompany)), equalTo(false))
    }

    @Test
    fun `Successful getAllCompanies Call`() {
        var newCompany = Company(
                name = "new",
                stockTickerSymbol = "symbol",
                description = "something",
                foundedYear = 1999L,
                urlLink = "www.link.com",
                urlLogo = "www.logo.com",
                csvDataPath = "something",
                readyForPrediction = true
        )
        newCompany = companyRepository.saveAndFlush(newCompany)


        val validLoginRequestDto = LoginRequestDto(
                email = "licenta@gmail.com",
                password = "12345678"
        )
        val request: HttpEntity<LoginRequestDto> = HttpEntity(validLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)
        val userToken = response.body!!.userToken
        headers.set(authorizationHeader, userToken)

        val requestGetAll: HttpEntity<Any> = HttpEntity(headers)
        val responseGetAll: ResponseEntity<List<CompanyDto>> =
                restTemplate.exchange("/api/company/get_all_companies", HttpMethod.GET, requestGetAll, object : ParameterizedTypeReference<List<CompanyDto>>() {})

        assertThat(responseGetAll.statusCode, equalTo(HttpStatus.OK))
        assertThat(responseGetAll.body!!.contains(companyDtoMapper.convertModelToDto(initialCompany)), equalTo(true))
        assertThat(responseGetAll.body!!.contains(companyDtoMapper.convertModelToDto(newCompany)), equalTo(true))
    }

    @Test
    fun `Successful addNewFavouriteUserCompany Call`() {
        var newCompany = Company(
                name = "new",
                stockTickerSymbol = "symbol",
                description = "something",
                foundedYear = 1999L,
                urlLink = "www.link.com",
                urlLogo = "www.logo.com",
                csvDataPath = "something",
                readyForPrediction = true
        )
        newCompany = companyRepository.saveAndFlush(newCompany)


        val validLoginRequestDto = LoginRequestDto(
                email = "licenta@gmail.com",
                password = "12345678"
        )
        val request: HttpEntity<LoginRequestDto> = HttpEntity(validLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)
        val userToken = response.body!!.userToken
        headers.set(authorizationHeader, userToken)

        val favouriteCompanyAdditionRequestDto = FavouriteCompanyAdditionRequestDto(
                companyId = newCompany.id
        )

        val requestAdd: HttpEntity<FavouriteCompanyAdditionRequestDto> = HttpEntity(favouriteCompanyAdditionRequestDto, headers)
        val responseAdd: ResponseEntity<FavouriteCompanyAdditionResponseDto> =
                restTemplate.postForEntity("/api/company/add_new_favourite_user_company", requestAdd, FavouriteCompanyAdditionResponseDto::class.java)

        assertThat(responseAdd.statusCode, equalTo(HttpStatus.OK))
        assertThat(responseAdd.body!!.message.contains("Successful addition of new favourite company"), equalTo(true))
    }

    @Test
    fun `Unsuccessful addNewFavouriteUserCompany Call`() {
        val validLoginRequestDto = LoginRequestDto(
                email = "licenta@gmail.com",
                password = "12345678"
        )
        val request: HttpEntity<LoginRequestDto> = HttpEntity(validLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)
        val userToken = response.body!!.userToken
        headers.set(authorizationHeader, userToken)

        val favouriteCompanyAdditionRequestDto = FavouriteCompanyAdditionRequestDto(
                companyId = initialCompany.id
        )

        val requestAdd: HttpEntity<FavouriteCompanyAdditionRequestDto> = HttpEntity(favouriteCompanyAdditionRequestDto, headers)
        val responseAdd: ResponseEntity<FavouriteCompanyAdditionResponseDto> =
                restTemplate.postForEntity("/api/company/add_new_favourite_user_company", requestAdd, FavouriteCompanyAdditionResponseDto::class.java)

        assertThat(responseAdd.statusCode, equalTo(HttpStatus.INTERNAL_SERVER_ERROR))
        assertThat(responseAdd.body!!.message.contains("Error in adding a new favourite company with message"), equalTo(true))
    }

    @Test
    fun `Successful removeFavouriteUserCompany Call`() {
        val validLoginRequestDto = LoginRequestDto(
                email = "licenta@gmail.com",
                password = "12345678"
        )
        val request: HttpEntity<LoginRequestDto> = HttpEntity(validLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)
        val userToken = response.body!!.userToken
        headers.set(authorizationHeader, userToken)

        val favouriteCompanyRemovalRequestDto = FavouriteCompanyRemovalRequestDto(
                companyId = initialCompany.id
        )

        val requestDelete: HttpEntity<FavouriteCompanyRemovalRequestDto> = HttpEntity(favouriteCompanyRemovalRequestDto, headers)
        val responseDelete: ResponseEntity<FavouriteCompanyRemovalResponseDto> =
                restTemplate.exchange("/api/company/remove_favourite_user_company", HttpMethod.DELETE, requestDelete, FavouriteCompanyRemovalResponseDto::class.java)

        assertThat(responseDelete.statusCode, equalTo(HttpStatus.OK))
        assertThat(responseDelete.body!!.message.contains("Successful removal of favourite company"), equalTo(true))
    }

    @Test
    fun `Unsuccessful removeFavouriteUserCompany Call`() {
        val validLoginRequestDto = LoginRequestDto(
                email = "licenta@gmail.com",
                password = "12345678"
        )
        val request: HttpEntity<LoginRequestDto> = HttpEntity(validLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)
        val userToken = response.body!!.userToken
        headers.set(authorizationHeader, userToken)

        val favouriteCompanyRemovalRequestDto = FavouriteCompanyRemovalRequestDto(
                companyId = -1L
        )

        val requestDelete: HttpEntity<FavouriteCompanyRemovalRequestDto> = HttpEntity(favouriteCompanyRemovalRequestDto, headers)
        val responseDelete: ResponseEntity<FavouriteCompanyRemovalResponseDto> =
                restTemplate.exchange("/api/company/remove_favourite_user_company", HttpMethod.DELETE, requestDelete, FavouriteCompanyRemovalResponseDto::class.java)

        assertThat(responseDelete.statusCode, equalTo(HttpStatus.INTERNAL_SERVER_ERROR))
        assertThat(responseDelete.body!!.message.contains("Error in removing the favourite company with message"), equalTo(true))
    }

    @Test
    fun `Unsuccessful saveNewStockMarketIndex Call`() {
        val validLoginRequestDto = LoginRequestDto(
                email = "admin@gmail.com",
                password = "12345678"
        )
        val request: HttpEntity<LoginRequestDto> = HttpEntity(validLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)
        val userToken = response.body!!.userToken
        headers.set(authorizationHeader, userToken)

        val stockMarketIndexAdditionRequestDto = StockMarketIndexAdditionRequestDto(
                name = "Sample name",
                stockTickerSymbol = "Some symbol",
                csvDataPath = "" //invalid csv data path
        )

        val requestSave: HttpEntity<StockMarketIndexAdditionRequestDto> = HttpEntity(stockMarketIndexAdditionRequestDto, headers)
        val responseSave: ResponseEntity<StockMarketIndexDto> = restTemplate.postForEntity("/api/company/admin/save_new_stock_market_index", requestSave, StockMarketIndexDto::class.java)

        assertThat(responseSave.statusCode, equalTo(HttpStatus.INTERNAL_SERVER_ERROR))
        assertThat(responseSave.body!!.stockMarketIndexId, equalTo(-1L))
    }

    @Test
    fun `Unsuccessful requiredPrediction Call`() {
        val validLoginRequestDto = LoginRequestDto(
                email = "licenta@gmail.com",
                password = "12345678"
        )
        val request: HttpEntity<LoginRequestDto> = HttpEntity(validLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)
        val userToken = response.body!!.userToken
        headers.set(authorizationHeader, userToken)

        val predictionRequestDto = PredictionRequestDto(
                companyId = initialCompany.id,
                predictionStartingDay = "2000-1-1" // invalid format
        )

        val requestPrediction: HttpEntity<PredictionRequestDto> = HttpEntity(predictionRequestDto, headers)
        val responsePrediction: ResponseEntity<PredictionResponseDto> = restTemplate.postForEntity("/api/company/require_prediction", requestPrediction, PredictionResponseDto::class.java)

        assertThat(responsePrediction.statusCode, equalTo(HttpStatus.INTERNAL_SERVER_ERROR))
        assertThat(responsePrediction.body!!.message.contains("Invalid date field"), equalTo(true))
    }

    @Test
    fun `Unsuccessful getHistoricalDataClosePrice Call`() {
        val validLoginRequestDto = LoginRequestDto(
                email = "licenta@gmail.com",
                password = "12345678"
        )
        val request: HttpEntity<LoginRequestDto> = HttpEntity(validLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)
        val userToken = response.body!!.userToken
        headers.set(authorizationHeader, userToken)

        val historicalDataClosePriceDto = HistoricalDataClosePriceDto(
                companyId = initialCompany.id,
                requiredCount = 22L // invalid required count
        )

        val requestPrice: HttpEntity<HistoricalDataClosePriceDto> = HttpEntity(historicalDataClosePriceDto, headers)
        val responsePrice: ResponseEntity<List<DateClosePrice>> =
                restTemplate.exchange("/api/company/get_historical_data_close_price", HttpMethod.POST, requestPrice, object : ParameterizedTypeReference<List<DateClosePrice>>() {})

        assertThat(responsePrice.statusCode, equalTo(HttpStatus.INTERNAL_SERVER_ERROR))
        assertThat(responsePrice.body!!.size, equalTo(0))
    }


    @AfterAll
    fun teardown() {
        favouriteCompanyRepository.deleteAll()
        companyRepository.deleteAll()
        stockMarketIndexRepository.deleteAll()
        userRepository.deleteAll()
    }
}
