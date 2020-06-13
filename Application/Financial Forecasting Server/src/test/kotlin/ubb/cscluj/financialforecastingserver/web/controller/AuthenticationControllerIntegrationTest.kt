package ubb.cscluj.financialforecastingserver.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.context.annotation.PropertySource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import ubb.cscluj.financialforecastingserver.core.model.User
import ubb.cscluj.financialforecastingserver.core.repository.UserRepository
import ubb.cscluj.financialforecastingserver.web.dto.*


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource("classpath:junit-platform.properties")
class AuthenticationControllerIntegrationTest(
        @Autowired val restTemplate: TestRestTemplate
) {
    @Autowired
    lateinit var userRepository: UserRepository

    private val authorizationHeader = "Authorization"

    private val objectMapper = ObjectMapper()
    lateinit var headers: HttpHeaders
    lateinit var initialUser: User
    lateinit var initialAdmin: User

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

    @Test
    fun `Unsuccessful Login Call`() {
        val invalidLoginRequestDto = LoginRequestDto(
                email = "invalid@gmail.com",
                password = "12345678"
        )

        val request: HttpEntity<LoginRequestDto> = HttpEntity(invalidLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)

        assertThat(response.statusCode, equalTo(HttpStatus.INTERNAL_SERVER_ERROR))
        assertThat(response.body!!.message, equalTo("Invalid combination sent for login"))
    }

    @Test
    fun `Successful Login Call`() {
        val validLoginRequestDto = LoginRequestDto(
                email = "licenta@gmail.com",
                password = "12345678"
        )

        val request: HttpEntity<LoginRequestDto> = HttpEntity(validLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)

        assertThat(response.statusCode, equalTo(HttpStatus.ACCEPTED))
        assertThat(response.body!!.message, equalTo("Successful login"))
    }

    @Test
    fun `Unsuccessful Register Call - user already exists`() {
        val invalidRegisterRequestDto = RegisterRequestDto(
                email = "licenta@gmail.com",
                password = "12345678"
        )

        val request: HttpEntity<RegisterRequestDto> = HttpEntity(invalidRegisterRequestDto, headers)
        val response = restTemplate.postForEntity("/api/register", request, RegisterResponseDto::class.java)

        assertThat(response.statusCode, equalTo(HttpStatus.INTERNAL_SERVER_ERROR))
        assertThat(response.body!!.message, equalTo("Email already found in the database"))
    }

    @Test
    fun `Unsuccessful Register Call - email validation fails`() {
        val invalidRegisterRequestDto = RegisterRequestDto(
                email = "licenta2@gmail.",
                password = "12345678"
        )

        val request: HttpEntity<RegisterRequestDto> = HttpEntity(invalidRegisterRequestDto, headers)
        val response = restTemplate.postForEntity("/api/register", request, RegisterResponseDto::class.java)

        assertThat(response.statusCode, equalTo(HttpStatus.INTERNAL_SERVER_ERROR))
        assertThat(response.body!!.message, equalTo("Invalid email field"))
    }

    @Test
    fun `Successful Register Call`() {
        val validRegisterRequestDto = RegisterRequestDto(
                email = "licenta_infinity@gmail.com",
                password = "12345678"
        )

        val request: HttpEntity<RegisterRequestDto> = HttpEntity(validRegisterRequestDto, headers)
        val response = restTemplate.postForEntity("/api/register", request, RegisterResponseDto::class.java)

        assertThat(response.statusCode, equalTo(HttpStatus.OK))
        assertThat(response.body!!.message, equalTo("Successful register"))
    }

    @Test
    fun `Unsuccessful Logout Call - unauthorized access`() {
        val invalidLogoutRequestDto = LogoutRequestDto(
                userToken = "ceva token"
        )

        headers.set(authorizationHeader, invalidLogoutRequestDto.userToken)
        val request: HttpEntity<LogoutRequestDto> = HttpEntity(invalidLogoutRequestDto, headers)
        val response = restTemplate.postForEntity("/api/user/logout", request, LogoutResponseDto::class.java)

        assertThat(response.statusCode, equalTo(HttpStatus.UNAUTHORIZED))
    }

    @Test
    fun `Successful Logout Call`() {
        //first we need to login
        val validLoginRequestDto = LoginRequestDto(
                email = "licenta@gmail.com",
                password = "12345678"
        )

        val request: HttpEntity<LoginRequestDto> = HttpEntity(validLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)

        val userToken = response.body!!.userToken
        val validLogoutRequestDto = LogoutRequestDto(
                userToken = userToken
        )
        headers.set(authorizationHeader, validLogoutRequestDto.userToken)

        val requestFinal: HttpEntity<LogoutRequestDto> = HttpEntity(validLogoutRequestDto, headers)
        val responseFinal = restTemplate.postForEntity("/api/user/logout", requestFinal, LogoutResponseDto::class.java)

        assertThat(responseFinal.statusCode, equalTo(HttpStatus.OK))
        assertThat(responseFinal.body!!.message, equalTo("Successful logout"))
    }


    @AfterAll
    fun teardown() {
        userRepository.deleteAll()
    }
}
