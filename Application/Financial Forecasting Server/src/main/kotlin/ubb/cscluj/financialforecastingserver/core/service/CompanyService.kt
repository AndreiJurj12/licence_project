package ubb.cscluj.financialforecastingserver.core.service

import ubb.cscluj.financialforecastingserver.core.exceptions.ExternalAPIFailedException
import ubb.cscluj.financialforecastingserver.core.exceptions.IdNotFoundException
import ubb.cscluj.financialforecastingserver.core.model.Company
import ubb.cscluj.financialforecastingserver.core.validator.ValidationException
import ubb.cscluj.financialforecastingserver.web.dto.CompanyAdditionRequestDto
import ubb.cscluj.financialforecastingserver.web.dto.CompanyUpdateRequestDto
import ubb.cscluj.financialforecastingserver.web.dto.FavouriteCompanyAdditionRequestDto
import ubb.cscluj.financialforecastingserver.web.dto.FavouriteCompanyRemovalRequestDto

interface CompanyService {
    @Throws(ValidationException::class, ExternalAPIFailedException::class)
    fun saveNewCompany(companyAdditionRequestDto: CompanyAdditionRequestDto): Company

    @Throws(ValidationException::class, IdNotFoundException::class)
    fun updateCompanyDetails(companyUpdateRequestDto: CompanyUpdateRequestDto): Company

    @Throws(ExternalAPIFailedException::class)
    fun updateAllStockData()

    @Throws(IdNotFoundException::class)
    fun getFavouriteUserCompanies(userId: Long): List<Company>

    fun getAllCompanies(): List<Company>

    @Throws(IdNotFoundException::class)
    fun addNewFavouriteUserCompany(favouriteCompanyAdditionRequestDto: FavouriteCompanyAdditionRequestDto, userId: Long)

    @Throws(IdNotFoundException::class)
    fun removeFavouriteUserCompany(favouriteCompanyRemovalRequestDto: FavouriteCompanyRemovalRequestDto, userId: Long)


}
