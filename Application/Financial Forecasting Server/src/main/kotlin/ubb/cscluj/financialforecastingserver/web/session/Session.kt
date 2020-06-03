package ubb.cscluj.financialforecastingserver.web.session

import java.time.Instant

data class Session(val userId: Long = -1,
                   val userToken: String = "",
                   val email: String = "",
                   val isAdmin: Boolean = false,
                   val sessionCreationTime: Instant = Instant.MIN)
