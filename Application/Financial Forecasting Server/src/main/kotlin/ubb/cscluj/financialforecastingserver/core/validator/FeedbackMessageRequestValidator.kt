package ubb.cscluj.financialforecastingserver.core.validator

import org.springframework.stereotype.Component
import ubb.cscluj.financialforecastingserver.web.dto.FeedbackMessageRequestDto

@Component
class FeedbackMessageRequestValidator : Validator<FeedbackMessageRequestDto> {

    override fun validate(entity: FeedbackMessageRequestDto) {
        if (entity.messageRequest.isBlank()) {
            throw ValidationException(invalidRequest)
        }
    }

    companion object ErrorMessages {
        const val invalidRequest: String = "Request was empty or null"
    }
}
