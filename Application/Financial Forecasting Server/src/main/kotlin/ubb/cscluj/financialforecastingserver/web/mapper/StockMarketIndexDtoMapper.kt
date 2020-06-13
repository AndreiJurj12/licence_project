package ubb.cscluj.financialforecastingserver.web.mapper

import org.springframework.stereotype.Component
import ubb.cscluj.financialforecastingserver.core.model.StockMarketIndex
import ubb.cscluj.financialforecastingserver.web.dto.StockMarketIndexDto

@Component
class StockMarketIndexDtoMapper : AbstractMapper<StockMarketIndex, StockMarketIndexDto>() {
    override fun convertDtoToModel(dto: StockMarketIndexDto): StockMarketIndex {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun convertModelToDto(model: StockMarketIndex): StockMarketIndexDto {
        return StockMarketIndexDto(
                stockMarketIndexId = model.id,
                name = model.name,
                stockTickerSymbol = model.stockTickerSymbol,
                csvDataPath = model.csvDataPath
        )
    }

}
