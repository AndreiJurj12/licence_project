package ubb.cscluj.financialforecastingserver.web.dto

data class LoginResponseDto(
        val message: String = "",
        val userToken: String = "",
        val isAdmin: Boolean = false,
        val userId: Long = -1)
