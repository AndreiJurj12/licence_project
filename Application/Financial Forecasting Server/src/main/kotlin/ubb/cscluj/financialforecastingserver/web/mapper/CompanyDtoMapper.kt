package ubb.cscluj.financialforecastingserver.web.mapper

import org.springframework.stereotype.Component
import ubb.cscluj.financialforecastingserver.core.model.Company
import ubb.cscluj.financialforecastingserver.web.dto.CompanyDto

@Component
class CompanyDtoMapper : AbstractMapper<Company, CompanyDto>() {
    override fun convertDtoToModel(dto: CompanyDto): Company {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun convertModelToDto(model: Company): CompanyDto {
        return CompanyDto(
                companyId = model.id,
                name = model.name,
                stockTickerSymbol = model.stockTickerSymbol,
                description = model.description,
                foundedYear = model.foundedYear,
                urlLink = model.urlLink,
                urlLogo = model.urlLogo,
                csvDataPath = model.csvDataPath,
                readyForPrediction = model.readyForPrediction
        )
    }

}
