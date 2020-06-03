package ubb.cscluj.financialforecastingserver.web.controller


import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import ubb.cscluj.financialforecastingserver.core.service.AuthenticationService
import ubb.cscluj.financialforecastingserver.web.dto.*
import ubb.cscluj.financialforecastingserver.web.session.Session

@RestController
@RequestMapping("/api")
class AuthenticationController @Autowired constructor(
        private val authenticationService: AuthenticationService
) {

    private var logging: Logger = LogManager.getLogger(AuthenticationController::class.java)

    @RequestMapping(value = ["/login"], method = [RequestMethod.POST])
    fun loginUser(@RequestBody loginRequestDto: LoginRequestDto):  ResponseEntity<LoginResponseDto> {
        logging.debug("Entering login user with email ${loginRequestDto.email}")
        return try {
            val newUserSession: Session = authenticationService.loginUser(loginRequestDto)
            val loginResponseDto = LoginResponseDto(
                    message = "Successful login",
                    userToken = newUserSession.userToken,
                    isAdmin = newUserSession.isAdmin,
                    userId = newUserSession.userId)

            logging.debug("Successful login for user with email ${newUserSession.email}")
            ResponseEntity(loginResponseDto, HttpStatus.ACCEPTED)
        } catch (exception: Exception) {
            logging.debug("Exception caught: ${exception.message}")
            val loginResponseDto = LoginResponseDto(
                    message = exception.message ?: "",
                    userToken = "",
                    isAdmin = false,
                    userId = -1
            )

            ResponseEntity(loginResponseDto, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @RequestMapping(value = ["/register"], method = [RequestMethod.POST])
    fun registerUser(@RequestBody registerRequestDto: RegisterRequestDto): ResponseEntity<RegisterResponseDto> {
        logging.debug("Entering register user with email ${registerRequestDto.email}")
        return try {
            authenticationService.registerUser(registerRequestDto)
            val registerResponseDto = RegisterResponseDto(
                    message = "Successful register"
            )

            logging.debug(registerResponseDto.message)
            ResponseEntity(registerResponseDto, HttpStatus.OK)
        } catch (exception: Exception) {
            logging.debug("Exception caught: ${exception.message}")
            val registerResponseDto = RegisterResponseDto(
                    message = exception.message ?: ""
            )

            ResponseEntity(registerResponseDto, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }


    @RequestMapping(value = ["/user/logout"], method = [RequestMethod.POST])
    fun logoutUser(@RequestBody logoutRequestDto: LogoutRequestDto): ResponseEntity<LogoutResponseDto> {
        logging.debug("Entering logout with userToken ${logoutRequestDto.userToken}")
        return try {
            authenticationService.logoutUser(logoutRequestDto.userToken)
            val logoutResponseDto = LogoutResponseDto(
                    message = "Successful logout"
            )

            logging.debug(logoutResponseDto.message)
            ResponseEntity(logoutResponseDto, HttpStatus.OK)
        } catch (exception: Exception) {
            logging.debug("Exception caught: ${exception.message}")
            val logoutResponseDto = LogoutResponseDto(
                    message = exception.message ?: ""
            )

            ResponseEntity(logoutResponseDto, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

}
