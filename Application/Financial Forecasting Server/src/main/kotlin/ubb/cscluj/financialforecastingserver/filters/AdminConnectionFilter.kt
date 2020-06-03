package ubb.cscluj.financialforecastingserver.filters

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import ubb.cscluj.financialforecastingserver.core.service.AuthenticationService
import ubb.cscluj.financialforecastingserver.web.session.Session
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
class AdminConnectionFilter @Autowired constructor(
        private val authenticationService: AuthenticationService
) : GenericFilterBean() {
    private var logging: Logger = LogManager.getLogger(AdminConnectionFilter::class.java)

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        val httpRequest = servletRequest as HttpServletRequest
        val httpResponse = servletResponse as HttpServletResponse

        if (httpRequest.method == corsMethod) {
            filterChain.doFilter(httpRequest, httpResponse)
            return
        }

        val userToken = httpRequest.getHeader(authorizationHeader)

        try {
            val userSession: Session? = authenticationService.getUserSession(userToken)
            if (userSession == null || !userSession.isAdmin) {
                logging.debug("${this.javaClass.simpleName}: doFilter()  - unauthorized access for non-admin token")
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
        const val authorizationHeader = "Authorization"
        const val corsMethod = "OPTIONS"
    }
}
