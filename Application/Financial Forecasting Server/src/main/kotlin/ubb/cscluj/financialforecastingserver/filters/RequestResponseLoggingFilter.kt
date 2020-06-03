package ubb.cscluj.financialforecastingserver.filters

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
class RequestResponseLoggingFilter : GenericFilterBean() {
    private var logging: Logger = LogManager.getLogger(RequestResponseLoggingFilter::class.java)


    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        val httpRequest = servletRequest as HttpServletRequest
        val httpResponse = servletResponse as HttpServletResponse
        logging.debug("Logging Request  ${httpRequest.method}, ${httpRequest.requestURI}")

        filterChain.doFilter(httpRequest, httpResponse)

        logging.debug("Logging Response : ${httpResponse.contentType}")
    }
}
