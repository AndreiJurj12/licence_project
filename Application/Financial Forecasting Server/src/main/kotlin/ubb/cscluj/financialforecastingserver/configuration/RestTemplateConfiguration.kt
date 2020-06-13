package ubb.cscluj.financialforecastingserver.configuration

import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.impl.client.HttpClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate


@Configuration
class RestTemplateConfiguration {
    @Bean
    fun getRestTemplate(): RestTemplate {
        val httpClient = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier()).build()

        val requestFactory = HttpComponentsClientHttpRequestFactory()
        requestFactory.httpClient = httpClient

        return RestTemplate(requestFactory)
    }
}
