package ubb.cscluj.financialforecastingserver.core.service

import org.springframework.stereotype.Service
import ubb.cscluj.financialforecastingserver.core.exceptions.LoginResponseException
import ubb.cscluj.financialforecastingserver.core.exceptions.RegisterResponseException
import ubb.cscluj.financialforecastingserver.web.dto.LoginRequestDto
import ubb.cscluj.financialforecastingserver.web.dto.RegisterRequestDto
import ubb.cscluj.financialforecastingserver.web.session.Session


interface AuthenticationService {

    fun isUserLoggedIn(userToken: String): Boolean

    @Throws(LoginResponseException::class)
    fun getUserSession(userToken: String?): Session?

    @Throws(LoginResponseException::class)
    fun loginUser(loginRequestDto: LoginRequestDto): Session

    @Throws(RegisterResponseException::class)
    fun registerUser(registerRequestDto: RegisterRequestDto)

    @Throws(LoginResponseException  ::class)
    fun logoutUser(userToken: String)
}
