package ubb.cscluj.financialforecasting.model.network_model

data class LoginResponse(
    val message: String = "",
    val userToken: String = "",
    val isAdmin: Boolean = false,
    val userId: Long = -1)