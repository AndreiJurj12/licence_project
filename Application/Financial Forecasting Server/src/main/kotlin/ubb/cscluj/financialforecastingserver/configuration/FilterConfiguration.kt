package ubb.cscluj.financialforecastingserver.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import ubb.cscluj.financialforecastingserver.filters.AdminConnectionFilter
import ubb.cscluj.financialforecastingserver.filters.RequestResponseLoggingFilter
import ubb.cscluj.financialforecastingserver.filters.UserConnectionFilter
import java.util.*


@Configuration
@ComponentScan(basePackages = ["ubb.cscluj.financialforecastingserver.filters"])
class FilterConfiguration @Autowired constructor(
        private val requestResponseLoggingFilter: RequestResponseLoggingFilter,
        private val userConnectionFilter: UserConnectionFilter,
        private val adminConnectionFilter: AdminConnectionFilter
) {

    @Bean
    fun loggingFilterFilterRegistrationBean(): FilterRegistrationBean<RequestResponseLoggingFilter> {
        val registrationBean = FilterRegistrationBean<RequestResponseLoggingFilter>()
        registrationBean.filter = requestResponseLoggingFilter
        registrationBean.order = 1
        registrationBean.addUrlPatterns("/*")
        return registrationBean
    }

    @Bean
    fun userConnectionFilterFilterRegistrationBean(): FilterRegistrationBean<UserConnectionFilter> {
        val registrationBean = FilterRegistrationBean<UserConnectionFilter>()
        registrationBean.filter = userConnectionFilter
        registrationBean.order = 2

        registrationBean.addUrlPatterns("/api/user/*")
        registrationBean.addUrlPatterns("/api/feedback/*")
        registrationBean.addUrlPatterns("/api/company/*")
        return registrationBean
    }

    @Bean
    fun adminConnectionFilterFilterRegistrationBean(): FilterRegistrationBean<AdminConnectionFilter> {
        val registrationBean = FilterRegistrationBean<AdminConnectionFilter>()
        registrationBean.filter = adminConnectionFilter
        registrationBean.order = 3

        registrationBean.addUrlPatterns("/api/user/admin/*")
        registrationBean.addUrlPatterns("/api/feedback/admin/*")
        registrationBean.addUrlPatterns("/api/company/admin/*")
        return registrationBean
    }

}
