package ubb.cscluj.financialforecastingserver.configuration

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfiguration {
    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate { // Do any additional configuration here
        return builder.build()
    }
}
