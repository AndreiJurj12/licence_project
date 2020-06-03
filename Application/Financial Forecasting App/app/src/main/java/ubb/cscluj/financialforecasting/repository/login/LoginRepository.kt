package ubb.cscluj.financialforecasting.repository.login

import com.google.gson.Gson
import ubb.cscluj.financialforecasting.model.network_model.LoginResponse
import ubb.cscluj.financialforecasting.model.network_model.RegisterRequest
import ubb.cscluj.financialforecasting.model.network_model.RegisterResponse
import ubb.cscluj.financialforecasting.model.User
import ubb.cscluj.financialforecasting.service.NetworkService
import java.net.HttpURLConnection


class LoginRepository(private val networkService: NetworkService) {

    suspend fun loginUser(user: User): LoginResponse {

        val response = networkService.service.login(user)
        if (response.raw().code == HttpURLConnection.HTTP_ACCEPTED) {
            return response.body() as LoginResponse
        }
        else {
            val loginResponse: LoginResponse =
                Gson().fromJson(response.errorBody()!!.charStream(), LoginResponse::class.java)
            throw LoginResponseException(
                loginResponse.message
            )
        }
    }

    suspend fun registerUser(registerRequest: RegisterRequest): RegisterResponse {

        val response = networkService.service.register(registerRequest)
        if (response.raw().code == HttpURLConnection.HTTP_OK) {
            return response.body() as RegisterResponse
        }
        else {
            val registerResponse =
                Gson().fromJson(response.errorBody()!!.charStream(), RegisterResponse::class.java)
            throw RegisterResponseException(
                registerResponse.message
            )
        }
    }
}