package ubb.cscluj.financialforecastingserver.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import ubb.cscluj.financialforecastingserver.configuration.FilterConfiguration
import ubb.cscluj.financialforecastingserver.core.model.User
import ubb.cscluj.financialforecastingserver.core.service.AuthenticationService
import ubb.cscluj.financialforecastingserver.web.dto.LoginRequestDto
import ubb.cscluj.financialforecastingserver.web.dto.LoginResponseDto
import ubb.cscluj.financialforecastingserver.web.dto.RegisterResponseDto
import ubb.cscluj.financialforecastingserver.web.session.Session
import java.time.Instant


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
    fun `login mock`() {
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
}

