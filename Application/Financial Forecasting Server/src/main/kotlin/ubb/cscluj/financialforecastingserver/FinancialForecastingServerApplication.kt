package ubb.cscluj.financialforecastingserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
class FinancialForecastingServerApplication



fun main(args: Array<String>) {
    runApplication<FinancialForecastingServerApplication>(*args)
}
