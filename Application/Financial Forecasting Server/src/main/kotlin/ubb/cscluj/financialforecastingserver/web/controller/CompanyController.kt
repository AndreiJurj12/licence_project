package ubb.cscluj.financialforecastingserver.web.controller

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ubb.cscluj.financialforecastingserver.core.service.AuthenticationService
import ubb.cscluj.financialforecastingserver.core.service.CompanyService
import ubb.cscluj.financialforecastingserver.web.dto.*
import ubb.cscluj.financialforecastingserver.web.mapper.CompanyDtoMapper

@RestController
@RequestMapping("/api/company")
class CompanyController @Autowired constructor(
        private val companyService: CompanyService,
        private val companyDtoMapper: CompanyDtoMapper,
        private val authenticationService: AuthenticationService
) {
    private var logging: Logger = LogManager.getLogger(CompanyController::class.java)

    @RequestMapping(value = ["/admin/save_new_company"], method = [RequestMethod.POST])
    fun saveNewCompany(@RequestHeader("Authorization") userToken: String,
                       @RequestBody companyAdditionRequestDto: CompanyAdditionRequestDto): ResponseEntity<CompanyDto> {
        logging.debug("Entering save new company with request $companyAdditionRequestDto")
        return try {
            val savedCompany = companyService.saveNewCompany(companyAdditionRequestDto)

            val savedCompanyDto = companyDtoMapper.convertModelToDto(savedCompany)
            logging.debug("Successful saveNewCompany with response: $savedCompanyDto")
            ResponseEntity(savedCompanyDto, HttpStatus.OK)
        } catch (exception: Exception) {
            logging.debug("Exception caught: ${exception.message}")

            val invalidCompanyDto = CompanyDto() //standard invalid data
            ResponseEntity(invalidCompanyDto, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @RequestMapping(value = ["/admin/update_company_details"], method = [RequestMethod.PUT])
    fun updateCompanyDetails(@RequestHeader("Authorization") userToken: String,
                             @RequestBody companyUpdateRequestDto: CompanyUpdateRequestDto): ResponseEntity<CompanyDto> {
        logging.debug("Entering update company details with request $companyUpdateRequestDto")
        return try {
            val updatedCompany = companyService.updateCompanyDetails(companyUpdateRequestDto)

            val updatedCompanyDto = companyDtoMapper.convertModelToDto(updatedCompany)
            logging.debug("Successful updateCompanyDetails with response: $updatedCompanyDto")
            ResponseEntity(updatedCompanyDto, HttpStatus.OK)
        } catch (exception: Exception) {
            logging.debug("Exception caught: ${exception.message}")

            val invalidCompanyDto = CompanyDto() //standard invalid data
            ResponseEntity(invalidCompanyDto, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @RequestMapping(value = ["/admin/update_all_stock_data"], method = [RequestMethod.POST])
    fun updateAllStockData(@RequestHeader("Authorization") userToken: String): ResponseEntity<UpdateAllStockDataResponseDto> {
        logging.debug("Entering update all stock data")
        return try {
            companyService.updateAllStockData()

            val updateAllStockDataResponseDto = UpdateAllStockDataResponseDto(
                    message = "Successful update for all stock data companies"
            )
            logging.debug("Successful updateAllStockData with response: $updateAllStockDataResponseDto")
            ResponseEntity(updateAllStockDataResponseDto, HttpStatus.OK)
        } catch (exception: Exception) {
            logging.debug("Exception caught: ${exception.message}")

            val updateAllStockDataResponseDto = UpdateAllStockDataResponseDto(
                    message = "Updating all stock data failed with error message: ${exception.message}"
            ) //standard invalid data
            ResponseEntity(updateAllStockDataResponseDto, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @RequestMapping(value = ["/get_favourite_user_companies"], method = [RequestMethod.GET])
    fun getFavouriteUserCompanies(@RequestHeader("Authorization") userToken: String): ResponseEntity<List<CompanyDto>> {
        logging.debug("Entering get favourite user companies")
        return try {
            val userId = authenticationService.getUserSession(userToken)?.userId ?: -1
            val favouriteCompanies = companyService.getFavouriteUserCompanies(userId)

            val favouriteCompaniesDto = ArrayList(companyDtoMapper.convertModelsToDtos(favouriteCompanies))
            logging.debug("Successful getFavouriteUserCompanies with response: $favouriteCompaniesDto")
            ResponseEntity(favouriteCompaniesDto, HttpStatus.OK)
        } catch (exception: Exception) {
            logging.debug("Exception caught: ${exception.message}")

            ResponseEntity(emptyList(), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @RequestMapping(value = ["/get_all_companies"], method = [RequestMethod.GET])
    fun getAllCompanies(@RequestHeader("Authorization") userToken: String): ResponseEntity<List<CompanyDto>> {
        logging.debug("Entering get all companies")
        return try {
            val allCompanies = companyService.getAllCompanies()

            val allCompaniesDto = ArrayList(companyDtoMapper.convertModelsToDtos(allCompanies))
            logging.debug("Successful getAllCompanies with response: $allCompaniesDto")
            ResponseEntity(allCompaniesDto, HttpStatus.OK)
        } catch (exception: Exception) {
            logging.debug("Exception caught: ${exception.message}")

            ResponseEntity(emptyList(), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @RequestMapping(value = ["/add_new_favourite_user_company"], method = [RequestMethod.POST])
    fun addNewFavouriteUserCompany(
            @RequestHeader("Authorization") userToken: String,
            @RequestBody favouriteCompanyAdditionRequestDto: FavouriteCompanyAdditionRequestDto): ResponseEntity<FavouriteCompanyAdditionResponseDto> {
        logging.debug("Entering add new favourite user company")
        return try {
            val userId = authenticationService.getUserSession(userToken)?.userId ?: -1
            companyService.addNewFavouriteUserCompany(favouriteCompanyAdditionRequestDto, userId)

            val favouriteCompanyAdditionResponseDto = FavouriteCompanyAdditionResponseDto(
                    message = "Successful addition of new favourite company"
            )
            logging.debug("Successful addNewFavouriteUserCompany with response: $favouriteCompanyAdditionResponseDto")
            ResponseEntity(favouriteCompanyAdditionResponseDto, HttpStatus.OK)
        } catch (exception: Exception) {
            logging.debug("Exception caught: ${exception.message}")

            val favouriteCompanyAdditionResponseDto = FavouriteCompanyAdditionResponseDto(
                    message = "Error in adding a new favourite company with message: ${exception.message}"
            )
            ResponseEntity(favouriteCompanyAdditionResponseDto, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @RequestMapping(value = ["/remove_favourite_user_company"], method = [RequestMethod.DELETE])
    fun removeFavouriteUserCompany(
            @RequestHeader("Authorization") userToken: String,
            @RequestBody favouriteCompanyRemovalRequestDto: FavouriteCompanyRemovalRequestDto): ResponseEntity<FavouriteCompanyRemovalResponseDto> {
        logging.debug("Entering remove favourite user company")
        return try {
            val userId = authenticationService.getUserSession(userToken)?.userId ?: -1
            companyService.removeFavouriteUserCompany(favouriteCompanyRemovalRequestDto, userId)

            val favouriteCompanyRemovalResponseDto = FavouriteCompanyRemovalResponseDto(
                    message = "Successful removal of favourite company"
            )
            logging.debug("Successful removeFavouriteUserCompany with response: $favouriteCompanyRemovalResponseDto")
            ResponseEntity(favouriteCompanyRemovalResponseDto, HttpStatus.OK)
        } catch (exception: Exception) {
            logging.debug("Exception caught: ${exception.message}")

            val favouriteCompanyRemovalResponseDto = FavouriteCompanyRemovalResponseDto(
                    message = "Error in removing the favourite company with message: ${exception.message}"
            )
            ResponseEntity(favouriteCompanyRemovalResponseDto, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
