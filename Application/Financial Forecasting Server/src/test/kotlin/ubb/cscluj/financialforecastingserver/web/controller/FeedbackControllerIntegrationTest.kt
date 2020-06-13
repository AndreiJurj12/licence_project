package ubb.cscluj.financialforecastingserver.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.PropertySource
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import ubb.cscluj.financialforecastingserver.core.model.FeedbackMessage
import ubb.cscluj.financialforecastingserver.core.model.User
import ubb.cscluj.financialforecastingserver.core.repository.FeedbackMessageRepository
import ubb.cscluj.financialforecastingserver.core.repository.UserRepository
import ubb.cscluj.financialforecastingserver.web.dto.*
import ubb.cscluj.financialforecastingserver.web.mapper.FeedbackMessageDtoMapper
import java.awt.Menu
import java.time.Instant


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource("classpath:junit-platform.properties")
class FeedbackControllerIntegrationTest(
        @Autowired val restTemplate: TestRestTemplate
) {
    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var feedbackMessageRepository: FeedbackMessageRepository

    @Autowired
    lateinit var feedbackMessageDtoMapper: FeedbackMessageDtoMapper

    private val authorizationHeader = "Authorization"

    private val objectMapper = ObjectMapper()
    lateinit var headers: HttpHeaders
    lateinit var initialUser: User
    lateinit var initialAdmin: User
    lateinit var initialMessageOne: FeedbackMessage
    lateinit var initialMessageTwo: FeedbackMessage

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
        initialMessageOne = FeedbackMessage(
                messageRequest = "Request 1",
                creationTime = Instant.now()
        )
        initialMessageOne.user = initialUser
        initialMessageOne = feedbackMessageRepository.save(initialMessageOne)

        initialMessageTwo = FeedbackMessage(
                messageRequest = "Request 2",
                creationTime = Instant.now()
        )
        initialMessageTwo.user = initialUser
        initialMessageTwo = feedbackMessageRepository.save(initialMessageTwo)
    }

    @AfterEach
    fun teardownMessages() {
        feedbackMessageRepository.deleteAll()
    }

    @Test
    fun `Successful getAllFeedbackMessages Call`() {
        val validLoginRequestDto = LoginRequestDto(
                email = "admin@gmail.com",
                password = "12345678"
        )
        val request: HttpEntity<LoginRequestDto> = HttpEntity(validLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)
        val userToken = response.body!!.userToken
        headers.set(authorizationHeader, userToken)

        val requestGetAll: HttpEntity<Any> = HttpEntity(headers)
        val responseGetAll: ResponseEntity<List<FeedbackMessageDto>> =
                restTemplate.exchange("/api/feedback/admin/all_feedback_messages", HttpMethod.GET, requestGetAll, object : ParameterizedTypeReference<List<FeedbackMessageDto>>() {})

        assertThat(responseGetAll.statusCode, equalTo(HttpStatus.OK))
        assertThat(responseGetAll.body!!.contains(feedbackMessageDtoMapper.convertModelToDto(initialMessageOne)), equalTo(true))
        assertThat(responseGetAll.body!!.contains(feedbackMessageDtoMapper.convertModelToDto(initialMessageTwo)), equalTo(true))
    }

    @Test
    fun `Successful getAllUserFeedbackMessages Call`() {
        val validLoginRequestDto = LoginRequestDto(
                email = "licenta@gmail.com",
                password = "12345678"
        )
        val request: HttpEntity<LoginRequestDto> = HttpEntity(validLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)
        val userToken = response.body!!.userToken
        headers.set(authorizationHeader, userToken)

        val requestGetAll: HttpEntity<Any> = HttpEntity(headers)
        val responseGetAll: ResponseEntity<List<FeedbackMessageDto>> =
                restTemplate.exchange("/api/feedback/all_user_feedback_messages", HttpMethod.GET, requestGetAll, object : ParameterizedTypeReference<List<FeedbackMessageDto>>() {})

        assertThat(responseGetAll.statusCode, equalTo(HttpStatus.OK))
        assertThat(responseGetAll.body!!.contains(feedbackMessageDtoMapper.convertModelToDto(initialMessageOne)), equalTo(true))
        assertThat(responseGetAll.body!!.contains(feedbackMessageDtoMapper.convertModelToDto(initialMessageTwo)), equalTo(true))
    }


    @Test
    fun `Successful saveNewUserFeedbackMessage Call`() {
        val validLoginRequestDto = LoginRequestDto(
                email = "licenta@gmail.com",
                password = "12345678"
        )
        val request: HttpEntity<LoginRequestDto> = HttpEntity(validLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)
        val userToken = response.body!!.userToken
        headers.set(authorizationHeader, userToken)

        val feedbackMessageRequestDto = FeedbackMessageRequestDto(
                messageRequest = "New Request"
        )

        val requestSave: HttpEntity<FeedbackMessageRequestDto> = HttpEntity(feedbackMessageRequestDto, headers)
        val responseSave: ResponseEntity<FeedbackMessageDto> = restTemplate.postForEntity("/api/feedback/save_new_user_feedback_message", requestSave, FeedbackMessageDto::class.java)

        assertThat(responseSave.statusCode, equalTo(HttpStatus.OK))
        assertThat(responseSave.body!!.messageRequest, equalTo(feedbackMessageRequestDto.messageRequest))
    }

    @Test
    fun `Unsuccessful saveNewUserFeedbackMessage Call`() {
        val validLoginRequestDto = LoginRequestDto(
                email = "licenta@gmail.com",
                password = "12345678"
        )
        val request: HttpEntity<LoginRequestDto> = HttpEntity(validLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)
        val userToken = response.body!!.userToken
        headers.set(authorizationHeader, userToken)

        val feedbackMessageRequestDto = FeedbackMessageRequestDto(
                messageRequest = ""
        )

        val requestSave: HttpEntity<FeedbackMessageRequestDto> = HttpEntity(feedbackMessageRequestDto, headers)
        val responseSave: ResponseEntity<FeedbackMessageDto> = restTemplate.postForEntity("/api/feedback/save_new_user_feedback_message", requestSave, FeedbackMessageDto::class.java)

        assertThat(responseSave.statusCode, equalTo(HttpStatus.INTERNAL_SERVER_ERROR))
        assertThat(responseSave.body!!.messageId, equalTo(-1L))
    }

    @Test
    fun `Successful updateResponseFeedbackMessage Call`() {
        val validLoginRequestDto = LoginRequestDto(
                email = "admin@gmail.com",
                password = "12345678"
        )
        val request: HttpEntity<LoginRequestDto> = HttpEntity(validLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)
        val userToken = response.body!!.userToken
        headers.set(authorizationHeader, userToken)

        val feedbackMessageRequestDto = FeedbackMessageRequestDto(
                messageRequest = "New Request"
        )

        val requestSave: HttpEntity<FeedbackMessageRequestDto> = HttpEntity(feedbackMessageRequestDto, headers)
        val responseSave: ResponseEntity<FeedbackMessageDto> = restTemplate.postForEntity("/api/feedback/save_new_user_feedback_message", requestSave, FeedbackMessageDto::class.java)

        assertThat(responseSave.statusCode, equalTo(HttpStatus.OK))
        assertThat(responseSave.body!!.messageRequest, equalTo(feedbackMessageRequestDto.messageRequest))

        val updateFeedbackMessageResponseDto = UpdateFeedbackMessageResponseDto(
                feedbackMessageId = responseSave.body!!.messageId,
                messageResponse = "Ceva raspuns"
        )

        val requestUpdate: HttpEntity<UpdateFeedbackMessageResponseDto> = HttpEntity(updateFeedbackMessageResponseDto, headers)
        val responseUpdate: ResponseEntity<FeedbackMessageDto> = restTemplate.exchange("/api/feedback/admin/update_response_feedback_message", HttpMethod.PUT, requestUpdate, FeedbackMessageDto::class.java)

        assertThat(responseUpdate.statusCode, equalTo(HttpStatus.OK))
        assertThat(responseUpdate.body!!.messageId, equalTo(updateFeedbackMessageResponseDto.feedbackMessageId))
        assertThat(responseUpdate.body!!.messageResponse, equalTo(updateFeedbackMessageResponseDto.messageResponse))
    }

    @Test
    fun `Unsuccessful updateResponseFeedbackMessage Call`() {
        val validLoginRequestDto = LoginRequestDto(
                email = "admin@gmail.com",
                password = "12345678"
        )
        val request: HttpEntity<LoginRequestDto> = HttpEntity(validLoginRequestDto, headers)
        val response = restTemplate.postForEntity("/api/login", request, LoginResponseDto::class.java)
        val userToken = response.body!!.userToken
        headers.set(authorizationHeader, userToken)

        val updateFeedbackMessageResponseDto = UpdateFeedbackMessageResponseDto(
                feedbackMessageId = initialMessageOne.id,
                messageResponse = ""
        )

        val requestUpdate: HttpEntity<UpdateFeedbackMessageResponseDto> = HttpEntity(updateFeedbackMessageResponseDto, headers)
        val responseUpdate: ResponseEntity<FeedbackMessageDto> = restTemplate.exchange("/api/feedback/admin/update_response_feedback_message", HttpMethod.PUT, requestUpdate, FeedbackMessageDto::class.java)

        assertThat(responseUpdate.statusCode, equalTo(HttpStatus.INTERNAL_SERVER_ERROR))
        assertThat(responseUpdate.body!!.messageId, equalTo(-1L))
    }

    @AfterAll
    fun teardown() {
        feedbackMessageRepository.deleteAll()
        userRepository.deleteAll()
    }
}
