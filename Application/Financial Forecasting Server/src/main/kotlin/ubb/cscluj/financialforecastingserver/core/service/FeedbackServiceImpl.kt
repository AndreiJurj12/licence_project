package ubb.cscluj.financialforecastingserver.core.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ubb.cscluj.financialforecastingserver.core.exceptions.IdNotFoundException
import ubb.cscluj.financialforecastingserver.core.model.FeedbackMessage
import ubb.cscluj.financialforecastingserver.core.model.User
import ubb.cscluj.financialforecastingserver.core.repository.FeedbackMessageRepository
import ubb.cscluj.financialforecastingserver.core.repository.UserRepository
import ubb.cscluj.financialforecastingserver.core.validator.FeedbackMessageRequestValidator
import ubb.cscluj.financialforecastingserver.core.validator.FeedbackMessageResponseValidator
import ubb.cscluj.financialforecastingserver.web.dto.FeedbackMessageRequestDto
import ubb.cscluj.financialforecastingserver.web.dto.UpdateFeedbackMessageResponseDto
import java.time.Instant

@Service
class FeedbackServiceImpl @Autowired constructor(
        val feedbackMessageRepository: FeedbackMessageRepository,
        val userRepository: UserRepository,
        val feedbackMessageRequestValidator: FeedbackMessageRequestValidator,
        val feedbackMessageResponseValidator: FeedbackMessageResponseValidator
) : FeedbackService{

    override fun saveNewFeedbackMessage(feedbackMessageRequestDto: FeedbackMessageRequestDto, userId: Long): FeedbackMessage {
        val user: User = userRepository.findByIdOrNull(userId)
                ?: throw IdNotFoundException("Id for user: $userId not found")

        feedbackMessageRequestValidator.validate(feedbackMessageRequestDto)

        val newFeedbackMessage = FeedbackMessage(
                messageRequest = feedbackMessageRequestDto.messageRequest,
                creationTime = Instant.now(),
                messageResponse = ""
        )
        newFeedbackMessage.user = user
        return feedbackMessageRepository.saveAndFlush(newFeedbackMessage)
    }

    @Transactional
    override fun updateFeedbackMessageResponse(updateFeedbackMessageResponseDto: UpdateFeedbackMessageResponseDto): FeedbackMessage {
        feedbackMessageResponseValidator.validate(updateFeedbackMessageResponseDto)

        val feedbackMessageId = updateFeedbackMessageResponseDto.feedbackMessageId

        val feedbackMessage = feedbackMessageRepository.findByIdOrNull(feedbackMessageId)
                ?: throw IdNotFoundException("Id for feedbackMessage: $feedbackMessageId not found")

        feedbackMessage.messageResponse = updateFeedbackMessageResponseDto.messageResponse
        return feedbackMessage
    }

    override fun getAllFeedbackMessagesByUser(userId: Long): List<FeedbackMessage> {
        val user: User = userRepository.findByIdOrNull(userId)
                ?: throw IdNotFoundException("Id for user: $userId not found")

        return feedbackMessageRepository.getFeedbackMessageByUser(user)
    }


    override fun getAllFeedbackMessages(): List<FeedbackMessage> = feedbackMessageRepository.findAll()
}
