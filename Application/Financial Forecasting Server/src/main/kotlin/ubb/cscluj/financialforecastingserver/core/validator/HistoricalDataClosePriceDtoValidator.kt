package ubb.cscluj.financialforecastingserver.core.validator

import org.springframework.stereotype.Component
import ubb.cscluj.financialforecastingserver.web.dto.HistoricalDataClosePriceDto

@Component
class HistoricalDataClosePriceDtoValidator : Validator<HistoricalDataClosePriceDto> {

    override fun validate(entity: HistoricalDataClosePriceDto) {
        if (entity.companyId == -1L) {
            throw ValidationException(invalidId)
        }

        if (!acceptedCountNumbers.contains(entity.requiredCount)) {
            throw ValidationException(invalidId)
        }
    }

    companion object ErrorMessages {
        private val acceptedCountNumbers = setOf<Long>(21, 63, 252)

        const val invalidId: String = "Invalid id field"
        val invalidCount: String = "Invalid count number - not in $acceptedCountNumbers"

    }
}
