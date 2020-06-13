package ubb.cscluj.financialforecastingserver.core.validator

import org.springframework.stereotype.Component
import ubb.cscluj.financialforecastingserver.web.dto.CompanyAdditionRequestDto
import ubb.cscluj.financialforecastingserver.web.dto.StockMarketIndexAdditionRequestDto
import java.time.Year

@Component
class StockMarketIndexAdditionRequestValidator : Validator<StockMarketIndexAdditionRequestDto> {
    override fun validate(entity: StockMarketIndexAdditionRequestDto) {
        if (entity.name.isBlank()) {
            throw ValidationException(invalidName)
        }
        if (entity.stockTickerSymbol.isBlank()) {
            throw ValidationException(invalidStockTickerSymbol)
        }
        if (entity.csvDataPath.isBlank()) {
            throw ValidationException(invalidCsvDataPath)
        }
    }

    companion object ErrorMessages {
        const val invalidName: String = "Invalid name field"
        const val invalidStockTickerSymbol: String = "Invalid stock ticker symbol field"
        const val invalidCsvDataPath: String = "Invalid csv data path field"
    }
}
