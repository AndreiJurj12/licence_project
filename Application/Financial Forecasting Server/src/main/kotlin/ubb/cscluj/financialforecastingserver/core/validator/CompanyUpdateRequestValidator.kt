package ubb.cscluj.financialforecastingserver.core.validator

import org.springframework.stereotype.Component
import ubb.cscluj.financialforecastingserver.web.dto.CompanyUpdateRequestDto
import java.time.Year

@Component
class CompanyUpdateRequestValidator : Validator<CompanyUpdateRequestDto> {
    override fun validate(entity: CompanyUpdateRequestDto) {
        if (entity.name.isBlank()) {
            throw ValidationException(invalidName)
        }
        if (entity.stockTickerSymbol.isBlank()) {
            throw ValidationException(invalidStockTickerSymbol)
        }
        if (entity.description.isBlank()) {
            throw ValidationException(invalidDescription)
        }
        if (entity.foundedYear < 1800 || entity.foundedYear > Year.now().value) {
            throw ValidationException(invalidFoundedYear)
        }
        if (entity.urlLink.isBlank()) {
            throw ValidationException(invalidUrlLink)
        }
        if (entity.urlLogo.isBlank()) {
            throw ValidationException(invalidUrlLogo)
        }
        if (entity.csvDataPath.isBlank()) {
            throw ValidationException(invalidCsvDataPath)
        }
    }

    companion object ErrorMessages {
        const val invalidName: String = "Invalid name field"
        const val invalidStockTickerSymbol: String = "Invalid stock ticker symbol field"
        const val invalidDescription: String = "Invalid description field"
        const val invalidFoundedYear: String = "Invalid founded year field"
        const val invalidUrlLink: String = "Invalid url link field"
        const val invalidUrlLogo: String = "Invalid url logo field"
        const val invalidCsvDataPath: String = "Invalid csv data path field"
    }
}
