package ubb.cscluj.financialforecastingserver.web.dto

import java.time.LocalDate

data class DateClosePrice(
        val date: LocalDate,
        val closePrice: Double
)
