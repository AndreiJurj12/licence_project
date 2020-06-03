package ubb.cscluj.financialforecastingserver.core.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ubb.cscluj.financialforecastingserver.core.exceptions.LoginResponseException
import ubb.cscluj.financialforecastingserver.core.exceptions.RegisterResponseException
import ubb.cscluj.financialforecastingserver.core.model.User
import ubb.cscluj.financialforecastingserver.core.repository.UserRepository
import ubb.cscluj.financialforecastingserver.core.validator.LoginRequestValidator
import ubb.cscluj.financialforecastingserver.core.validator.RegisterRequestValidator
import ubb.cscluj.financialforecastingserver.web.dto.LoginRequestDto
import ubb.cscluj.financialforecastingserver.web.dto.RegisterRequestDto
import ubb.cscluj.financialforecastingserver.web.session.Session
import java.time.Instant
import java.util.*


@Service
class AuthenticationServiceImpl @Autowired constructor(
        val userRepository: UserRepository,
        val loginRequestValidator: LoginRequestValidator,
        val registerRequestValidator: RegisterRequestValidator
): AuthenticationService {
    private val connectedUsers: MutableMap<String, Session> = mutableMapOf()

    private fun generateNewUserToken(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    override fun isUserLoggedIn(userToken: String): Boolean {
        return connectedUsers.containsKey(userToken)
    }

    override fun getUserSession(userToken: String?): Session? {
        if (connectedUsers.containsKey(userToken)) {
            return connectedUsers[userToken]
        } else {
            throw LoginResponseException("Invalid user token!")
        }
    }


    override fun loginUser(loginRequestDto: LoginRequestDto): Session {
        loginRequestValidator.validate(loginRequestDto)

        val user: User? = userRepository.getUserByEmail(loginRequestDto.email)
        if (user == null || (user.password != loginRequestDto.password)) {
            throw LoginResponseException("Invalid combination sent for login")
        }

        val userToken = generateNewUserToken()
        val newSession = Session(
                userId = user.id,
                userToken = userToken,
                email = user.email,
                isAdmin = user.isAdmin,
                sessionCreationTime = Instant.now())

        //add user to connected
        connectedUsers[userToken] = newSession
        return newSession
    }

    override fun registerUser(registerRequestDto: RegisterRequestDto) {
        registerRequestValidator.validate(registerRequestDto)

        val user: User? = userRepository.getUserByEmail(registerRequestDto.email)
        if (user != null) {
            throw RegisterResponseException("Email already found in the database")
        }

        val newUser = User(
                email = registerRequestDto.email,
                password = registerRequestDto.password,
                isAdmin = false)
        userRepository.saveAndFlush(newUser)
    }

    override fun logoutUser(userToken: String) {
        if (!isUserLoggedIn(userToken)) {
            throw LoginResponseException("User was not logged in previously")
        }
        connectedUsers.remove(userToken)
    }
}
