package ubb.cscluj.financialforecastingserver.filters

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import ubb.cscluj.financialforecastingserver.core.service.AuthenticationService
import java.io.IOException
import java.time.Duration
import java.time.Instant
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
class UserConnectionFilter @Autowired constructor(
        private val authenticationService: AuthenticationService
) : GenericFilterBean() {
    private var logging: Logger = LogManager.getLogger(UserConnectionFilter::class.java)

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        val httpRequest = servletRequest as HttpServletRequest
        val httpResponse = servletResponse as HttpServletResponse

        if (httpRequest.method == corsMethod) {
            filterChain.doFilter(httpRequest, httpResponse)
            return
        }

        val userToken: String? = httpRequest.getHeader(authorizationHeader)
        if (userToken == null) {
            logging.debug("${this.javaClass.simpleName}: doFilter() No $authorizationHeader header!")
            httpResponse.status = HttpStatus.UNAUTHORIZED.value()
            return
        }

        logging.debug("User token received: $userToken")
        if (!authenticationService.isUserLoggedIn(userToken)) {
            logging.debug("${this.javaClass.simpleName}: doFilter() Invalid token!")
            httpResponse.status = HttpStatus.UNAUTHORIZED.value()
            return
        }

        //now we check for expiration
        try {
            val sessionCreationTime = authenticationService.getUserSession(userToken)!!.sessionCreationTime
            val currentTime = Instant.now()
            val duration: Duration = Duration.between(sessionCreationTime, currentTime)
            if (duration.seconds > numberSecondsToExpire) {
                authenticationService.logoutUser(userToken)
                logging.debug("${this.javaClass.simpleName}: doFilter() Session has expired!")
                httpResponse.status = HttpStatus.UNAUTHORIZED.value()
                return
            }
        } catch (e: Exception) {
            logging.debug("${this.javaClass.simpleName}: doFilter() Exception: ${e.message}")
            e.printStackTrace()
            httpResponse.status = HttpStatus.INTERNAL_SERVER_ERROR.value()
            return
        }
        //we are logged in move forward
        filterChain.doFilter(httpRequest, httpResponse)
    }

    companion object CompanionObject {
        const val numberSecondsToExpire = 24 * 60 * 60 //1 day
        const val authorizationHeader = "Authorization"
        const val corsMethod = "OPTIONS"
    }
}
