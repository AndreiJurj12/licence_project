package ubb.cscluj.financialforecastingserver.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.natpryce.hamkrest.containsSubstring
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import ubb.cscluj.financialforecastingserver.configuration.FilterConfiguration
import ubb.cscluj.financialforecastingserver.core.exceptions.LoginResponseException
import ubb.cscluj.financialforecastingserver.core.model.User
import ubb.cscluj.financialforecastingserver.core.service.AuthenticationService
import ubb.cscluj.financialforecastingserver.filters.UserConnectionFilter
import ubb.cscluj.financialforecastingserver.web.dto.*
import ubb.cscluj.financialforecastingserver.web.session.Session
import java.time.Instant
import javax.xml.bind.ValidationException


@WebMvcTest(controllers = [AuthenticationController::class])
@ContextConfiguration(classes = [FilterConfiguration::class, AuthenticationController::class])
class AuthenticationControllerHttpTest {
    @Autowired
    private lateinit var mapper: ObjectMapper

    @MockkBean
    private lateinit var authenticationService: AuthenticationService

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `login mock successful`() {
        val initialUser = User(email = "licenta@gmail.com", password = "12345678", isAdmin = false)
        val loginRequestDto = LoginRequestDto(email = "licenta@gmail.com", password = "12345678")
        val session = Session(userId = 1,
                userToken = "token",
                email = initialUser.email,
                isAdmin = initialUser.isAdmin,
                sessionCreationTime = Instant.now())
        val loginResponseDto = LoginResponseDto(message = "Successful login", userToken = "token", isAdmin = false, userId = 1)
        val stringLoginResponseDto = mapper.writeValueAsString(loginResponseDto)

        every { authenticationService.loginUser(loginRequestDto) } returns session

        mockMvc.post("/api/login") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(loginRequestDto)
        }.andDo { print() }
                .andExpect {
            status { isAccepted }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(stringLoginResponseDto) }
        }
    }

    @Test
    fun `login mock unsuccessful`() {
        val loginRequestDto = LoginRequestDto(email = "fake_email", password = "12345678")
        every { authenticationService.loginUser(loginRequestDto) }
                .throws(ValidationException("Invalid email field"))

        val loginResponseDto = LoginResponseDto(
                message = "Invalid email field",
                userToken = "",
                isAdmin = false,
                userId = -1
        )

        mockMvc.post("/api/login") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(loginRequestDto)
        }.andExpect {
            status { isInternalServerError }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(loginResponseDto)) }
        }
    }

    @Test
    fun `register mock successful`() {
        val registerRequestDto = RegisterRequestDto(
                email = "fake_email",
                password = "12345678"
        )
        val registerResponseDto = RegisterResponseDto(
                message = "Successful register"
        )

        every { authenticationService.registerUser(registerRequestDto) } returns Unit

        mockMvc.post("/api/register") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(registerRequestDto)
        }.andExpect {
            status { isOk }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(registerResponseDto)) }
        }
    }

    @Test
    fun `register mock unsuccessful`() {
        val registerRequestDto = RegisterRequestDto(
                email = "test@gmail.com",
                password = "12345678"
        )
        val registerResponseDto = RegisterResponseDto(
                message = "Invalid email field"
        )

        every { authenticationService.registerUser(registerRequestDto) }
                .throws(ValidationException("Invalid email field"))

        mockMvc.post("/api/register") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(registerRequestDto)
        }.andExpect {
            status { isInternalServerError }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(registerResponseDto)) }
        }
    }

    @Test
    fun `logout mock successful`() {
        val logoutRequestDto = LogoutRequestDto(
                userToken = "token_1234"
        )
        val logoutResponseDto = LogoutResponseDto(
                message = "Successful logout"
        )
        val sampleSession = Session(
                userId = 1,
                userToken = logoutRequestDto.userToken,
                isAdmin = false,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )


        every { authenticationService.isUserLoggedIn(logoutRequestDto.userToken) } returns true
        every { authenticationService.getUserSession(logoutRequestDto.userToken) } returns sampleSession
        every { authenticationService.logoutUser(logoutRequestDto.userToken) } returns Unit

        mockMvc.post("/api/user/logout") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(logoutRequestDto)
            header("Authorization", logoutRequestDto.userToken)
        }.andExpect {
            status { isOk }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(logoutResponseDto)) }
        }
    }

    @Test
    fun `logout mock unsuccessful`() {
        val logoutRequestDto = LogoutRequestDto(
                userToken = "token_1234"
        )
        val logoutResponseDto = LogoutResponseDto(
                message = "User was not logged in previously"
        )
        val sampleSession = Session(
                userId = 1,
                userToken = logoutRequestDto.userToken,
                isAdmin = false,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )


        every { authenticationService.isUserLoggedIn(logoutRequestDto.userToken) } returns true
        every { authenticationService.getUserSession(logoutRequestDto.userToken) } returns sampleSession
        every { authenticationService.logoutUser(logoutRequestDto.userToken) }
                .throws(LoginResponseException("User was not logged in previously"))

        mockMvc.post("/api/user/logout") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(logoutRequestDto)
            header("Authorization", logoutRequestDto.userToken)
        }.andExpect {
            status { isInternalServerError }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(logoutResponseDto)) }
        }
    }
}

