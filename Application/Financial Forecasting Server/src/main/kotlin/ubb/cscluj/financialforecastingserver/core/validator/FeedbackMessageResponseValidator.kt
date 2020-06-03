package ubb.cscluj.financialforecastingserver.core.validator

import org.springframework.stereotype.Component
import ubb.cscluj.financialforecastingserver.web.dto.UpdateFeedbackMessageResponseDto

@Component
class FeedbackMessageResponseValidator : Validator<UpdateFeedbackMessageResponseDto> {

    override fun validate(entity: UpdateFeedbackMessageResponseDto) {
        if (entity.messageResponse.isBlank()) {
            throw ValidationException(invalidResponse)
        }

        if (entity.feedbackMessageId == -1L) {
            throw ValidationException(invalidId)
        }
    }

    companion object ErrorMessages {
        const val invalidResponse: String = "Response was empty or null"
        const val invalidId: String = "Feedback message Id was -1 - invalid"
    }
}
