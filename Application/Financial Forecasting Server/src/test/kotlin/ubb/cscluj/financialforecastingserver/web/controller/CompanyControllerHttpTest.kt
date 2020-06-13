package ubb.cscluj.financialforecastingserver.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.apache.tomcat.jni.Local
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.*
import ubb.cscluj.financialforecastingserver.configuration.FilterConfiguration
import ubb.cscluj.financialforecastingserver.core.exceptions.ExternalAPIFailedException
import ubb.cscluj.financialforecastingserver.core.exceptions.IdNotFoundException
import ubb.cscluj.financialforecastingserver.core.model.Company
import ubb.cscluj.financialforecastingserver.core.model.StockMarketIndex
import ubb.cscluj.financialforecastingserver.core.service.AuthenticationService
import ubb.cscluj.financialforecastingserver.core.service.CompanyService
import ubb.cscluj.financialforecastingserver.web.dto.*
import ubb.cscluj.financialforecastingserver.web.mapper.CompanyDtoMapper
import ubb.cscluj.financialforecastingserver.web.mapper.StockMarketIndexDtoMapper
import ubb.cscluj.financialforecastingserver.web.session.Session
import java.time.Instant
import java.time.LocalDate
import javax.xml.bind.ValidationException


@WebMvcTest(controllers = [CompanyController::class])
@ContextConfiguration(classes = [FilterConfiguration::class, CompanyController::class, CompanyDtoMapper::class, StockMarketIndexDtoMapper::class])
class CompanyControllerHttpTest {
    @Autowired
    private lateinit var mapper: ObjectMapper
    @Autowired
    private lateinit var mockMvc: MockMvc
    @MockkBean
    private lateinit var authenticationService: AuthenticationService
    @MockkBean
    private lateinit var companyService: CompanyService


    @Test
    fun `saveNewCompany mock successful`() {
        val userToken: String = "token_1234"
        val sampleSession = Session(
                userId = 1,
                userToken = userToken,
                isAdmin = true,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val sampleCompanyAdditionRequestDto = CompanyAdditionRequestDto(
                name = "Name",
                stockTickerSymbol = "Symbol",
                description = "Description",
                foundedYear = 2000L,
                urlLink = "www.link.com",
                urlLogo = "www.logo.com",
                csvDataPath = "C:://path"
        )
        val sampleCompany = Company(
                name = "Name",
                stockTickerSymbol = "Symbol",
                description = "Description",
                foundedYear = 2000L,
                urlLink = "www.link.com",
                urlLogo = "www.logo.com",
                csvDataPath = "C:://path",
                readyForPrediction = false
        )
        val sampleCompanyDto = CompanyDto(
                companyId = 0L,
                name = "Name",
                stockTickerSymbol = "Symbol",
                description = "Description",
                foundedYear = 2000L,
                urlLink = "www.link.com",
                urlLogo = "www.logo.com",
                csvDataPath = "C:://path",
                readyForPrediction = false
        )




        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { companyService.saveNewCompany(sampleCompanyAdditionRequestDto) } returns sampleCompany

        mockMvc.post("/api/company/admin/save_new_company") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(sampleCompanyAdditionRequestDto)
            header("Authorization", userToken)
        }.andExpect {
            status { isOk }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(sampleCompanyDto)) }
        }
    }

    @Test
    fun `saveNewCompany mock unsuccessful`() {
        val userToken: String = "token_1234"
        val sampleSession = Session(
                userId = 1,
                userToken = userToken,
                isAdmin = true,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val sampleCompanyAdditionRequestDto = CompanyAdditionRequestDto(
                name = "Name",
                stockTickerSymbol = "Symbol",
                description = "Description",
                foundedYear = 2000L,
                urlLink = "www.link.com",
                urlLogo = "www.logo.com",
                csvDataPath = "invalid_path"
        )
        val sampleInvalidCompanyDto = CompanyDto()


        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { companyService.saveNewCompany(sampleCompanyAdditionRequestDto) }
                .throws(ValidationException("Invalid csv data path field"))

        mockMvc.post("/api/company/admin/save_new_company") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(sampleCompanyAdditionRequestDto)
            header("Authorization", userToken)
        }.andExpect {
            status { isInternalServerError }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(sampleInvalidCompanyDto)) }
        }
    }

    @Test
    fun `updateCompanyDetails mock successful`() {
        val userToken: String = "token_1234"
        val sampleSession = Session(
                userId = 1,
                userToken = userToken,
                isAdmin = true,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val sampleCompanyUpdateRequestDto = CompanyUpdateRequestDto(
                companyId = 1L,
                name = "Name",
                stockTickerSymbol = "Symbol",
                description = "Description",
                foundedYear = 2000L,
                urlLink = "www.link.com",
                urlLogo = "www.logo.com",
                csvDataPath = "C:://path",
                readyForPrediction = true
        )
        val sampleCompany = Company(
                name = "Name",
                stockTickerSymbol = "Symbol",
                description = "Description",
                foundedYear = 2000L,
                urlLink = "www.link.com",
                urlLogo = "www.logo.com",
                csvDataPath = "C:://path",
                readyForPrediction = true

        )
        sampleCompany.id = 1L
        val sampleCompanyDto = CompanyDto(
                companyId = 1L,
                name = "Name",
                stockTickerSymbol = "Symbol",
                description = "Description",
                foundedYear = 2000L,
                urlLink = "www.link.com",
                urlLogo = "www.logo.com",
                csvDataPath = "C:://path",
                readyForPrediction = true
        )




        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { companyService.updateCompanyDetails(sampleCompanyUpdateRequestDto) } returns sampleCompany

        mockMvc.put("/api/company/admin/update_company_details") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(sampleCompanyUpdateRequestDto)
            header("Authorization", userToken)
        }.andExpect {
            status { isOk }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(sampleCompanyDto)) }
        }
    }

    @Test
    fun `updateCompanyDetails mock unsuccessful`() {
        val userToken: String = "token_1234"
        val sampleSession = Session(
                userId = 1,
                userToken = userToken,
                isAdmin = true,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val sampleCompanyUpdateRequestDto = CompanyUpdateRequestDto(
                companyId = 1L,
                name = "Name",
                stockTickerSymbol = "Symbol",
                description = "Description",
                foundedYear = 2000L,
                urlLink = "www.link.com",
                urlLogo = "www.logo.com",
                csvDataPath = "invalid_path",
                readyForPrediction = true
        )
        val sampleInvalidCompanyDto = CompanyDto()


        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { companyService.updateCompanyDetails(sampleCompanyUpdateRequestDto) }
                .throws(ValidationException("Invalid csv data path field"))

        mockMvc.put("/api/company/admin/update_company_details") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(sampleCompanyUpdateRequestDto)
            header("Authorization", userToken)
        }.andExpect {
            status { isInternalServerError }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(sampleInvalidCompanyDto)) }
        }
    }

    @Test
    fun `updateAllStockData mock successful`() {
        val userToken: String = "token_1234"
        val sampleSession = Session(
                userId = 1,
                userToken = userToken,
                isAdmin = true,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )

        val sampleUpdateAllStockDataResponseDto = UpdateAllStockDataResponseDto(
                message = "Successful update for all stock data companies"
        )

        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { companyService.updateAllStockData() } returns Unit

        mockMvc.post("/api/company/admin/update_all_stock_data") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = ""
            header("Authorization", userToken)
        }.andExpect {
            status { isOk }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(sampleUpdateAllStockDataResponseDto)) }
        }
    }

    @Test
    fun `updateAllStockData mock unsuccessful`() {
        val userToken: String = "token_1234"
        val sampleSession = Session(
                userId = 1,
                userToken = userToken,
                isAdmin = true,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val sampleUpdateAllStockDataResponseDto = UpdateAllStockDataResponseDto(
                message = "Updating all stock data failed with error message: Failed External API"
        )


        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { companyService.updateAllStockData() }
                .throws(ExternalAPIFailedException("Failed External API"))

        mockMvc.post("/api/company/admin/update_all_stock_data") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = ""
            header("Authorization", userToken)
        }.andExpect {
            status { isInternalServerError }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(sampleUpdateAllStockDataResponseDto)) }
        }
    }

    @Test
    fun `getFavouriteUserCompanies mock successful`() {
        val userToken: String = "token_1234"
        val sampleSession = Session(
                userId = 1,
                userToken = userToken,
                isAdmin = false,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val sampleCompany = Company(
                name = "Name",
                stockTickerSymbol = "Symbol",
                description = "Description",
                foundedYear = 2000L,
                urlLink = "www.link.com",
                urlLogo = "www.logo.com",
                csvDataPath = "C:://path",
                readyForPrediction = true

        )
        sampleCompany.id = 1L
        val sampleCompanyDto = CompanyDto(
                companyId = 1L,
                name = "Name",
                stockTickerSymbol = "Symbol",
                description = "Description",
                foundedYear = 2000L,
                urlLink = "www.link.com",
                urlLogo = "www.logo.com",
                csvDataPath = "C:://path",
                readyForPrediction = true
        )


        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { companyService.getFavouriteUserCompanies(sampleSession.userId) } returns arrayListOf(sampleCompany)

        mockMvc.get("/api/company/get_favourite_user_companies") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = ""
            header("Authorization", userToken)
        }.andExpect {
            status { isOk }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(arrayListOf(sampleCompanyDto))) }
        }
    }

    @Test
    fun `getFavouriteUserCompanies mock unsuccessful`() {
        val userToken: String = "token_1234"
        val userId = 1L
        val sampleSession = Session(
                userId = userId,
                userToken = userToken,
                isAdmin = true,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )


        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { companyService.getFavouriteUserCompanies(userId) }
                .throws(IdNotFoundException("Id for user: $userId not found"))

        mockMvc.get("/api/company/get_favourite_user_companies") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = ""
            header("Authorization", userToken)
        }.andExpect {
            status { isInternalServerError }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json("[]") }
        }
    }

    @Test
    fun `getAllCompanies mock successful`() {
        val userToken: String = "token_1234"
        val sampleSession = Session(
                userId = 1,
                userToken = userToken,
                isAdmin = false,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val sampleCompany = Company(
                name = "Name",
                stockTickerSymbol = "Symbol",
                description = "Description",
                foundedYear = 2000L,
                urlLink = "www.link.com",
                urlLogo = "www.logo.com",
                csvDataPath = "C:://path",
                readyForPrediction = true

        )
        sampleCompany.id = 1L
        val sampleCompanyDto = CompanyDto(
                companyId = 1L,
                name = "Name",
                stockTickerSymbol = "Symbol",
                description = "Description",
                foundedYear = 2000L,
                urlLink = "www.link.com",
                urlLogo = "www.logo.com",
                csvDataPath = "C:://path",
                readyForPrediction = true
        )


        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { companyService.getAllCompanies() } returns arrayListOf(sampleCompany)

        mockMvc.get("/api/company/get_all_companies") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = ""
            header("Authorization", userToken)
        }.andExpect {
            status { isOk }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(arrayListOf(sampleCompanyDto))) }
        }
    }

    @Test
    fun `getAllCompanies mock unsuccessful`() {
        val userToken: String = "token_1234"
        val userId = 1L
        val sampleSession = Session(
                userId = userId,
                userToken = userToken,
                isAdmin = true,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )


        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { companyService.getAllCompanies() }
                .throws(Exception("Some exception while processing"))

        mockMvc.get("/api/company/get_all_companies") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = ""
            header("Authorization", userToken)
        }.andExpect {
            status { isInternalServerError }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json("[]") }
        }
    }

    @Test
    fun `addNewFavouriteUserCompany mock successful`() {
        val userToken: String = "token_1234"
        val userId = 1L
        val sampleSession = Session(
                userId = userId,
                userToken = userToken,
                isAdmin = false,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val sampleFavouriteCompanyAdditionRequestDto = FavouriteCompanyAdditionRequestDto(
                companyId = 1L
        )
        val sampleFavouriteCompanyAdditionResponseDto = FavouriteCompanyAdditionResponseDto(
                message = "Successful addition of new favourite company"
        )

        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { companyService.addNewFavouriteUserCompany(sampleFavouriteCompanyAdditionRequestDto, userId) } returns Unit

        mockMvc.post("/api/company/add_new_favourite_user_company") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(sampleFavouriteCompanyAdditionRequestDto)
            header("Authorization", userToken)
        }.andExpect {
            status { isOk }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(sampleFavouriteCompanyAdditionResponseDto)) }
        }
    }

    @Test
    fun `addNewFavouriteUserCompany mock unsuccessful`() {
        val userToken: String = "token_1234"
        val userId = 1L
        val sampleSession = Session(
                userId = userId,
                userToken = userToken,
                isAdmin = true,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val sampleFavouriteCompanyAdditionRequestDto = FavouriteCompanyAdditionRequestDto(
                companyId = 1L
        )
        val sampleFavouriteCompanyAdditionResponseDto = FavouriteCompanyAdditionResponseDto(
                message = "Error in adding a new favourite company with message: Id for user: $userId not found"
        )

        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { companyService.addNewFavouriteUserCompany(sampleFavouriteCompanyAdditionRequestDto, userId) }
                .throws(IdNotFoundException("Id for user: $userId not found"))

        mockMvc.post("/api/company/add_new_favourite_user_company") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(sampleFavouriteCompanyAdditionRequestDto)
            header("Authorization", userToken)
        }.andExpect {
            status { isInternalServerError }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(sampleFavouriteCompanyAdditionResponseDto)) }
        }
    }

    @Test
    fun `removeFavouriteUserCompany mock successful`() {
        val userToken: String = "token_1234"
        val userId = 1L
        val sampleSession = Session(
                userId = userId,
                userToken = userToken,
                isAdmin = false,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val sampleFavouriteCompanyRemovalRequestDto = FavouriteCompanyRemovalRequestDto(
                companyId = 1L
        )
        val sampleFavouriteCompanyRemovalResponseDto = FavouriteCompanyRemovalResponseDto(
                message = "Successful removal of favourite company"
        )

        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { companyService.removeFavouriteUserCompany(sampleFavouriteCompanyRemovalRequestDto, userId) } returns Unit

        mockMvc.delete("/api/company/remove_favourite_user_company") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(sampleFavouriteCompanyRemovalRequestDto)
            header("Authorization", userToken)
        }.andExpect {
            status { isOk }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(sampleFavouriteCompanyRemovalResponseDto)) }
        }
    }

    @Test
    fun `removeFavouriteUserCompany mock unsuccessful`() {
        val userToken: String = "token_1234"
        val userId = 1L
        val sampleSession = Session(
                userId = userId,
                userToken = userToken,
                isAdmin = true,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val sampleFavouriteCompanyRemovalRequestDto = FavouriteCompanyRemovalRequestDto(
                companyId = 1L
        )
        val sampleFavouriteCompanyRemovalResponseDto = FavouriteCompanyRemovalResponseDto(
                message = "Error in removing the favourite company with message: Id for user: $userId not found"
        )
        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { companyService.removeFavouriteUserCompany(sampleFavouriteCompanyRemovalRequestDto, userId) }
                .throws(IdNotFoundException("Id for user: $userId not found"))

        mockMvc.delete("/api/company/remove_favourite_user_company") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(sampleFavouriteCompanyRemovalRequestDto)
            header("Authorization", userToken)
        }.andExpect {
            status { isInternalServerError }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(sampleFavouriteCompanyRemovalResponseDto)) }
        }
    }

    @Test
    fun `saveNewStockMarketIndex mock successful`() {
        val userToken: String = "token_1234"
        val sampleSession = Session(
                userId = 1,
                userToken = userToken,
                isAdmin = true,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val sampleStockMarketIndexAdditionRequestDto = StockMarketIndexAdditionRequestDto(
                name = "Name",
                stockTickerSymbol = "Symbol",
                csvDataPath = "C:://path"
        )
        val sampleStockMarketIndex = StockMarketIndex(
                name = "Name",
                stockTickerSymbol = "Symbol",
                csvDataPath = "C:://path"
        )
        val sampleStockMarketIndexDto = StockMarketIndexDto(
                stockMarketIndexId = 0L,
                name = "Name",
                stockTickerSymbol = "Symbol",
                csvDataPath = "C:://path"
        )




        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { companyService.saveNewStockMarketIndex(sampleStockMarketIndexAdditionRequestDto) } returns sampleStockMarketIndex

        mockMvc.post("/api/company/admin/save_new_stock_market_index") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(sampleStockMarketIndexAdditionRequestDto)
            header("Authorization", userToken)
        }.andExpect {
            status { isOk }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(sampleStockMarketIndexDto)) }
        }
    }

    @Test
    fun `saveNewStockMarketIndex mock unsuccessful`() {
        val userToken: String = "token_1234"
        val sampleSession = Session(
                userId = 1,
                userToken = userToken,
                isAdmin = true,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val sampleStockMarketIndexAdditionRequestDto = StockMarketIndexAdditionRequestDto(
                name = "Name",
                stockTickerSymbol = "Symbol",
                csvDataPath = "invalid_path"
        )
        val sampleInvalidStockMarketIndexDto = StockMarketIndexDto()


        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { companyService.saveNewStockMarketIndex(sampleStockMarketIndexAdditionRequestDto) }
                .throws(ValidationException("Invalid csv data path field"))

        mockMvc.post("/api/company/admin/save_new_stock_market_index") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(sampleStockMarketIndexAdditionRequestDto)
            header("Authorization", userToken)
        }.andExpect {
            status { isInternalServerError }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(sampleInvalidStockMarketIndexDto)) }
        }
    }

    @Test
    fun `requiredPrediction mock successful`() {
        val userToken: String = "token_1234"
        val sampleSession = Session(
                userId = 1,
                userToken = userToken,
                isAdmin = true,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val samplePredictionRequestDto = PredictionRequestDto(
                companyId = 1L,
                predictionStartingDay = "2018-01-01"
        )
        val samplePredictionResponseDto = PredictionResponseDto(
                message = "Success",
                classificationResult = "Increase",
                regressionResult = 200.0,
                expectedPredictedPrice = null,
                historicalClosePrice = emptyList()
        )


        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { companyService.predictionRequest(samplePredictionRequestDto) } returns samplePredictionResponseDto

        mockMvc.post("/api/company/require_prediction") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(samplePredictionRequestDto)
            header("Authorization", userToken)
        }.andExpect {
            status { isOk }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(samplePredictionResponseDto)) }
        }
    }

    @Test
    fun `requiredPrediction mock unsuccessful`() {
        val userToken: String = "token_1234"
        val sampleSession = Session(
                userId = 1,
                userToken = userToken,
                isAdmin = true,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val samplePredictionRequestDto = PredictionRequestDto(
                companyId = 1L,
                predictionStartingDay = "2018-00-00"
        )
        val sampleInvalidPredictionResponseDto = PredictionResponseDto(
                message = "Invalid starting prediction date field"
        )


        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { companyService.predictionRequest(samplePredictionRequestDto) }
                .throws(ValidationException("Invalid starting prediction date field"))

        mockMvc.post("/api/company/require_prediction") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(samplePredictionRequestDto)
            header("Authorization", userToken)
        }.andExpect {
            status { isInternalServerError }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(sampleInvalidPredictionResponseDto)) }
        }
    }

    @Test
    fun `getHistoricalDataClosePrice mock successful`() {
        val userToken: String = "token_1234"
        val sampleSession = Session(
                userId = 1,
                userToken = userToken,
                isAdmin = true,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val sampleHistoricalDataClosePriceDto = HistoricalDataClosePriceDto(
                companyId = 1L,
                requiredCount = 21L
        )

        val sampleListDataClosePrice = arrayListOf(
                DateClosePrice(closePrice = 200.0, date = LocalDate.now()),
                DateClosePrice(closePrice = 190.0, date = LocalDate.now().minusDays(1))
        )

        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { companyService.getHistoricalDataClosePrice(sampleHistoricalDataClosePriceDto) } returns sampleListDataClosePrice

        mockMvc.post("/api/company/get_historical_data_close_price") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(sampleHistoricalDataClosePriceDto)
            header("Authorization", userToken)
        }.andExpect {
            status { isOk }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(sampleListDataClosePrice)) }
        }
    }

    @Test
    fun `getHistoricalDataClosePrice mock unsuccessful`() {
        val userToken: String = "token_1234"
        val sampleSession = Session(
                userId = 1,
                userToken = userToken,
                isAdmin = true,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val sampleHistoricalDataClosePriceDto = HistoricalDataClosePriceDto(
                companyId = 1L,
                requiredCount = 22L
        )


        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { companyService.getHistoricalDataClosePrice(sampleHistoricalDataClosePriceDto) }
                .throws(ValidationException("Invalid count number"))

        mockMvc.post("/api/company/get_historical_data_close_price") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(sampleHistoricalDataClosePriceDto)
            header("Authorization", userToken)
        }.andExpect {
            status { isInternalServerError }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json("[]") }
        }
    }
}
