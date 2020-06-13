package ubb.cscluj.financialforecastingserver.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import ubb.cscluj.financialforecastingserver.configuration.FilterConfiguration
import ubb.cscluj.financialforecastingserver.core.exceptions.IdNotFoundException
import ubb.cscluj.financialforecastingserver.core.exceptions.LoginResponseException
import ubb.cscluj.financialforecastingserver.core.model.FeedbackMessage
import ubb.cscluj.financialforecastingserver.core.model.User
import ubb.cscluj.financialforecastingserver.core.service.AuthenticationService
import ubb.cscluj.financialforecastingserver.core.service.FeedbackService
import ubb.cscluj.financialforecastingserver.web.dto.*
import ubb.cscluj.financialforecastingserver.web.mapper.FeedbackMessageDtoMapper
import ubb.cscluj.financialforecastingserver.web.session.Session
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@WebMvcTest(controllers = [FeedbackController::class])
@ContextConfiguration(classes = [FilterConfiguration::class, FeedbackController::class, FeedbackMessageDtoMapper::class])
class FeedbackControllerHttpTest {
    @Autowired
    private lateinit var mapper: ObjectMapper

    @MockkBean
    private lateinit var authenticationService: AuthenticationService

    @MockkBean
    private lateinit var feedbackService: FeedbackService

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `getAllFeedbackMessages mock successful`() {
        val userToken: String = "token_1234"
        val sampleSession = Session(
                userId = 1,
                userToken = userToken,
                isAdmin = true,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val sampleCreationTime = Instant.now()
        val dateFormat = "dd-MM-yyyy HH:mm:ss"
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat)
                .withLocale(Locale.UK)
                .withZone(ZoneId.systemDefault())

        val sampleFeedbackMessage = FeedbackMessage(
                messageRequest = "Request",
                creationTime = sampleCreationTime,
                messageResponse = "Response"
        )
        val sampleFeedbackMessageDto = FeedbackMessageDto(
                messageId = 0,
                messageRequest = "Request",
                creationTime = formatter.format(sampleCreationTime),
                messageResponse = "Response",
                userId = -1
        )


        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { feedbackService.getAllFeedbackMessages() } returns arrayListOf(sampleFeedbackMessage)

        mockMvc.get("/api/feedback/admin/all_feedback_messages") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = ""
            header("Authorization", userToken)
        }.andExpect {
            status { isOk }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(arrayListOf(sampleFeedbackMessageDto))) }
        }
    }

    @Test
    fun `getlAllFeedbackMessages mock unsuccessful`() {
        val userToken: String = "token_1234"
        val sampleSession = Session(
                userId = 1,
                userToken = userToken,
                isAdmin = true,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )

        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { feedbackService.getAllFeedbackMessages() }
                .throws(Exception("error"))

        mockMvc.get("/api/feedback/admin/all_feedback_messages") {
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
    fun `getAllUserFeedbackMessages mock successful`() {
        val userToken: String = "token_1234"
        val sampleSession = Session(
                userId = 1,
                userToken = userToken,
                isAdmin = true,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val sampleCreationTime = Instant.now()
        val dateFormat = "dd-MM-yyyy HH:mm:ss"
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat)
                .withLocale(Locale.UK)
                .withZone(ZoneId.systemDefault())

        val sampleFeedbackMessage = FeedbackMessage(
                messageRequest = "Request",
                creationTime = sampleCreationTime,
                messageResponse = "Response"
        )
        val sampleFeedbackMessageDto = FeedbackMessageDto(
                messageId = 0,
                messageRequest = "Request",
                creationTime = formatter.format(sampleCreationTime),
                messageResponse = "Response",
                userId = -1
        )
        val userId = 1L


        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { feedbackService.getAllFeedbackMessagesByUser(userId) } returns arrayListOf(sampleFeedbackMessage)

        mockMvc.get("/api/feedback/all_user_feedback_messages") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = ""
            header("Authorization", userToken)
        }.andExpect {
            status { isOk }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(arrayListOf(sampleFeedbackMessageDto))) }
        }
    }

    @Test
    fun `getAllUserFeedbackMessages mock unsuccessful`() {
        val userToken: String = "token_1234"
        val sampleSession = Session(
                userId = 1,
                userToken = userToken,
                isAdmin = false,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val userId = 1L

        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { feedbackService.getAllFeedbackMessagesByUser(userId) }
                .throws(IdNotFoundException("Id for user: $userId not found"))

        mockMvc.get("/api/feedback/all_user_feedback_messages") {
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
    fun `saveNewUserFeedbackMessage mock successful`() {
        val userToken: String = "token_1234"
        val sampleSession = Session(
                userId = 1,
                userToken = userToken,
                isAdmin = false,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val userId = 1L
        val sampleUser = User("email@yy", "1234", true)
        sampleUser.id = userId

        val sampleCreationTime = Instant.now()
        val dateFormat = "dd-MM-yyyy HH:mm:ss"
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat)
                .withLocale(Locale.UK)
                .withZone(ZoneId.systemDefault())

        val sampleFeedbackMessage = FeedbackMessage(
                messageRequest = "Request",
                creationTime = sampleCreationTime,
                messageResponse = "Response"
        )
        sampleFeedbackMessage.user = sampleUser

        val sampleFeedbackMessageDto = FeedbackMessageDto(
                messageId = 0,
                messageRequest = "Request",
                creationTime = formatter.format(sampleCreationTime),
                messageResponse = "Response",
                userId = userId
        )

        val sampleFeedbackMessageRequest = FeedbackMessageRequestDto(
                messageRequest = "Request"
        )



        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { feedbackService.saveNewFeedbackMessage(sampleFeedbackMessageRequest, userId) } returns sampleFeedbackMessage

        mockMvc.post("/api/feedback/save_new_user_feedback_message") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(sampleFeedbackMessageRequest)
            header("Authorization", userToken)
        }.andExpect {
            status { isOk }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(sampleFeedbackMessageDto)) }
        }
    }

    @Test
    fun `saveNewUserFeedbackMessage mock unsuccessful`() {
        val userToken: String = "token_1234"
        val sampleSession = Session(
                userId = 1,
                userToken = userToken,
                isAdmin = false,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val userId = 1L
        val sampleFeedbackMessageRequest = FeedbackMessageRequestDto(
                messageRequest = "Request"
        )
        val invalidFeedbackMessageDto = FeedbackMessageDto(
                -1,
                "",
                "invalid date",
                "",
                -1
        )


        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { feedbackService.saveNewFeedbackMessage(sampleFeedbackMessageRequest, userId) }
                .throws(IdNotFoundException("Id for user: $userId not found"))

        mockMvc.post("/api/feedback/save_new_user_feedback_message") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(sampleFeedbackMessageRequest)
            header("Authorization", userToken)
        }.andExpect {
            status { isInternalServerError }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(invalidFeedbackMessageDto)) }
        }
    }

    @Test
    fun `updateResponseFeedbackMessage mock successful`() {
        val userToken: String = "token_1234"
        val sampleSession = Session(
                userId = 1,
                userToken = userToken,
                isAdmin = true,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val userId = 1L
        val sampleUser = User("email@yy", "1234", true)
        sampleUser.id = userId

        val sampleCreationTime = Instant.now()
        val dateFormat = "dd-MM-yyyy HH:mm:ss"
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat)
                .withLocale(Locale.UK)
                .withZone(ZoneId.systemDefault())

        val sampleFeedbackMessage = FeedbackMessage(
                messageRequest = "Request",
                creationTime = sampleCreationTime,
                messageResponse = "Response"
        )
        sampleFeedbackMessage.user = sampleUser

        val sampleFeedbackMessageDto = FeedbackMessageDto(
                messageId = 0,
                messageRequest = "Request",
                creationTime = formatter.format(sampleCreationTime),
                messageResponse = "Response",
                userId = userId
        )

        val updateFeedbackMessageResponseDto = UpdateFeedbackMessageResponseDto(
                feedbackMessageId = 1L,
                messageResponse = "Response"
        )



        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { feedbackService.updateFeedbackMessageResponse(updateFeedbackMessageResponseDto) } returns sampleFeedbackMessage

        mockMvc.put("/api/feedback/admin/update_response_feedback_message") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(updateFeedbackMessageResponseDto)
            header("Authorization", userToken)
        }.andExpect {
            status { isOk }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(sampleFeedbackMessageDto)) }
        }
    }

    @Test
    fun `updateResponseFeedbackMessage mock unsuccessful`() {
        val userToken: String = "token_1234"
        val sampleSession = Session(
                userId = 1,
                userToken = userToken,
                isAdmin = true,
                email = "ceva pe acolo",
                sessionCreationTime = Instant.now()
        )
        val invalidFeedbackMessageDto = FeedbackMessageDto(
                -1,
                "",
                "invalid date",
                "",
                -1
        )
        val updateFeedbackMessageResponseDto = UpdateFeedbackMessageResponseDto(
                feedbackMessageId = 1L,
                messageResponse = "Response"
        )


        every { authenticationService.isUserLoggedIn(userToken) } returns true
        every { authenticationService.getUserSession(userToken) } returns sampleSession
        every { feedbackService.updateFeedbackMessageResponse(updateFeedbackMessageResponseDto) }
                .throws(IdNotFoundException("Id for feedbackMessage: 1 not found"))

        mockMvc.put("/api/feedback/admin/update_response_feedback_message") {
            contentType = MediaType.APPLICATION_JSON
            characterEncoding = "utf-8"
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(updateFeedbackMessageResponseDto)
            header("Authorization", userToken)
        }.andExpect {
            status { isInternalServerError }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(invalidFeedbackMessageDto)) }
        }
    }
}

