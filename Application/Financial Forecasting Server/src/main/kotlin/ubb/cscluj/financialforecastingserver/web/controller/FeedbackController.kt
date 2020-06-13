package ubb.cscluj.financialforecastingserver.web.controller

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ubb.cscluj.financialforecastingserver.core.service.AuthenticationService
import ubb.cscluj.financialforecastingserver.core.service.FeedbackService
import ubb.cscluj.financialforecastingserver.web.dto.FeedbackMessageDto
import ubb.cscluj.financialforecastingserver.web.dto.FeedbackMessageRequestDto
import ubb.cscluj.financialforecastingserver.web.dto.UpdateFeedbackMessageResponseDto
import ubb.cscluj.financialforecastingserver.web.mapper.FeedbackMessageDtoMapper

@RestController
@RequestMapping("/api/feedback")
class FeedbackController @Autowired constructor(
        private val authenticationService: AuthenticationService,
        private val feedbackService: FeedbackService,
        private val feedbackMessageDtoMapper: FeedbackMessageDtoMapper
) {
    private var logging: Logger = LogManager.getLogger(FeedbackController::class.java)


    @RequestMapping(value = ["/admin/all_feedback_messages"], method = [RequestMethod.GET])
    fun getAllFeedbackMessages(@RequestHeader("Authorization") userToken: String): ResponseEntity<List<FeedbackMessageDto>> {
        logging.debug("Entering get all feedback messages")

        return try {
            val allFeedbackMessages = feedbackService.getAllFeedbackMessages()

            val allFeedbackMessagesDto: List<FeedbackMessageDto> =
                    ArrayList(feedbackMessageDtoMapper.convertModelsToDtos(allFeedbackMessages))
            logging.debug("Successful getAllFeedbackMessages with length: ${allFeedbackMessagesDto.size}")
            ResponseEntity(allFeedbackMessagesDto, HttpStatus.OK)
        } catch (exception: Exception) {
            logging.debug("Exception caught: ${exception.message}")
            ResponseEntity(emptyList(), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @RequestMapping(value = ["/all_user_feedback_messages"], method = [RequestMethod.GET])
    fun getAllUserFeedbackMessages(@RequestHeader("Authorization") userToken: String): ResponseEntity<List<FeedbackMessageDto>> {
        logging.debug("Entering get all user feedback messages")

        return try {
            val userId = authenticationService.getUserSession(userToken)?.userId ?: -1
            val allFeedbackMessages = feedbackService.getAllFeedbackMessagesByUser(userId)

            val allFeedbackMessagesDto: List<FeedbackMessageDto> =
                    ArrayList(feedbackMessageDtoMapper.convertModelsToDtos(allFeedbackMessages))
            logging.debug("Successful getAllUserFeedbackMessages with length: ${allFeedbackMessagesDto.size}")
            ResponseEntity(allFeedbackMessagesDto, HttpStatus.OK)
        } catch (exception: Exception) {
            logging.debug("Exception caught: ${exception.message}")
            ResponseEntity(emptyList(), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @RequestMapping(value = ["/save_new_user_feedback_message"], method = [RequestMethod.POST])
    fun saveNewUserFeedbackMessage(@RequestHeader("Authorization") userToken: String,
                                   @RequestBody feedbackMessageRequestDto: FeedbackMessageRequestDto): ResponseEntity<FeedbackMessageDto> {
        logging.debug("Entering save new user feedback message with request $feedbackMessageRequestDto")
        return try {
            val userId = authenticationService.getUserSession(userToken)?.userId ?: -1
            val savedFeedbackMessage = feedbackService.saveNewFeedbackMessage(feedbackMessageRequestDto, userId)

            val savedFeedbackMessageDto = feedbackMessageDtoMapper.convertModelToDto(savedFeedbackMessage)
            logging.debug("Successful saveNewUserMessage with response: $savedFeedbackMessageDto")
            ResponseEntity(savedFeedbackMessageDto, HttpStatus.OK)
        } catch (exception: Exception) {
            logging.debug("Exception caught: ${exception.message}")

            val invalidFeedbackMessageDto = FeedbackMessageDto(
                    -1,
                    "",
                    "invalid date",
                    "",
                    -1
            )
            ResponseEntity(invalidFeedbackMessageDto, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }


    @RequestMapping(value = ["/admin/update_response_feedback_message"], method = [RequestMethod.PUT])
    fun updateResponseFeedbackMessage(
            @RequestHeader("Authorization") userToken: String,
            @RequestBody updateFeedbackMessageResponseDto: UpdateFeedbackMessageResponseDto): ResponseEntity<FeedbackMessageDto> {
        logging.debug("Entering update response feedback message with request $updateFeedbackMessageResponseDto")
        return try {
            val updatedFeedbackMessage = feedbackService.updateFeedbackMessageResponse(updateFeedbackMessageResponseDto)

            val updatedFeedbackMessageDto = feedbackMessageDtoMapper.convertModelToDto(updatedFeedbackMessage)
            logging.debug("Successful updateResponseFeedbackMessage with response: $updatedFeedbackMessageDto")
            ResponseEntity(updatedFeedbackMessageDto, HttpStatus.OK)
        } catch (exception: Exception) {
            logging.debug("Exception caught: $exception")
            logging.debug("Exception caught: ${exception.message}")

            val invalidUpdateFeedbackMessageDto = FeedbackMessageDto(
                    -1,
                    "",
                    "invalid date",
                    "",
                    -1
            )
            ResponseEntity(invalidUpdateFeedbackMessageDto, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
