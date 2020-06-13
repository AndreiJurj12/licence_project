package ubb.cscluj.financialforecastingserver.core.validator

import org.springframework.stereotype.Component
import ubb.cscluj.financialforecastingserver.web.dto.PredictionRequestDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Component
class PredictionRequestValidator : Validator<PredictionRequestDto> {
    override fun validate(entity: PredictionRequestDto) {
        if (entity.companyId == -1L) {
            throw ValidationException(invalidId)
        }

        try {
            LocalDate.parse(entity.predictionStartingDay, formatter)
        }
        catch (exception: DateTimeParseException) {
            throw ValidationException(invalidDateField)
        }
    }

    companion object ErrorMessages {
        const val invalidId: String = "Invalid id field"
        const val invalidDateField: String = "Invalid date field"

        private const val dateFormat = "yyyy-MM-dd"
        var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat)
    }
}
