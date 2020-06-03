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
import ubb.cscluj.financialforecastingserver.web.dto.LoginRequestDto
import ubb.cscluj.financialforecastingserver.web.dto.LoginResponseDto
import ubb.cscluj.financialforecastingserver.web.dto.RegisterRequestDto
import ubb.cscluj.financialforecastingserver.web.dto.RegisterResponseDto


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource("classpath:junit-platform.properties")
class AuthenticationControllerIntegrationTest(
        @Autowired val restTemplate: TestRestTemplate
) {
    private val objectMapper = ObjectMapper()
    lateinit var headers: HttpHeaders
    lateinit var loginRequestDto: LoginRequestDto
    lateinit var initialUser: User

    @BeforeAll
    fun setup() {
        headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        initialUser = User(email = "licenta@gmail.com", password = "12345678", isAdmin = false)
        //entityManager.persist(initialUser)
        //entityManager.flush()

        loginRequestDto = LoginRequestDto(email = "licenta@gmail.com", password = "12345678")
    }

    @Test
    fun `Unsuccessful Login Call`() {
        val request: HttpEntity<LoginRequestDto> = HttpEntity(loginRequestDto, headers)

        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)

        assertThat(response.statusCode, equalTo(HttpStatus.INTERNAL_SERVER_ERROR))
        assertThat(response.body!!.message, equalTo("Invalid combination sent for login"))
    }


    @AfterAll
    fun teardown() {
        //entityManager.remove(initialUser)
    }
}
