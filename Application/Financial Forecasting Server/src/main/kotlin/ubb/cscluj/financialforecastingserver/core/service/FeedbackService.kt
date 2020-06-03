package ubb.cscluj.financialforecastingserver.core.service

import org.springframework.stereotype.Service
import ubb.cscluj.financialforecastingserver.core.exceptions.IdNotFoundException
import ubb.cscluj.financialforecastingserver.core.model.FeedbackMessage
import ubb.cscluj.financialforecastingserver.core.validator.ValidationException
import ubb.cscluj.financialforecastingserver.web.dto.FeedbackMessageRequestDto
import ubb.cscluj.financialforecastingserver.web.dto.UpdateFeedbackMessageResponseDto

interface FeedbackService {
    @Throws(ValidationException::class, IdNotFoundException::class)
    fun saveNewFeedbackMessage(feedbackMessageRequestDto: FeedbackMessageRequestDto, userId: Long): FeedbackMessage

    @Throws(ValidationException::class, IdNotFoundException::class)
    fun updateFeedbackMessageResponse(updateFeedbackMessageResponseDto: UpdateFeedbackMessageResponseDto): FeedbackMessage

    @Throws(IdNotFoundException::class)
    fun getAllFeedbackMessagesByUser(userId: Long): List<FeedbackMessage>

    fun getAllFeedbackMessages(): List<FeedbackMessage>
}
