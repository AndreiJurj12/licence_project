package ubb.cscluj.financialforecasting.repository.logout

import com.google.gson.Gson
import ubb.cscluj.financialforecasting.model.network_model.LogoutRequest
import ubb.cscluj.financialforecasting.model.network_model.LogoutResponse
import ubb.cscluj.financialforecasting.service.NetworkService
import java.net.HttpURLConnection

class LogoutRepository(private val networkService: NetworkService) {
    suspend fun logoutUser(logoutRequest: LogoutRequest): LogoutResponse {

        val response = networkService.service.logout(
            logoutRequest.userToken,
            logoutRequest
        )
        if (response.raw().code == HttpURLConnection.HTTP_OK) {
            return response.body() as LogoutResponse
        } else {
            val logoutResponse: LogoutResponse =
                Gson().fromJson(response.errorBody()!!.charStream(), LogoutResponse::class.java)
            throw LogoutResponseException(
                logoutResponse.message
            )
        }
    }
}