package ubb.cscluj.financialforecasting.client.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ubb.cscluj.financialforecasting.MainApplication
import ubb.cscluj.financialforecasting.client.viewmodel.exposed_states.ClientExposedLoadedDetailTaskState
import ubb.cscluj.financialforecasting.model.Company
import ubb.cscluj.financialforecasting.model.network_model.PredictionResponseDto
import ubb.cscluj.financialforecasting.repository.company.CompanyRepository
import ubb.cscluj.financialforecasting.utils.logd

class ClientCompanyPredictionResultViewModel(application: Application) : AndroidViewModel(application) {
    private val userToken: String
    private val companyRepository: CompanyRepository
    private val _clientExposedLoadedDetailTaskState: MutableLiveData<ClientExposedLoadedDetailTaskState> = MutableLiveData()
    private val _clientExposedLoadedPredictionTaskState: MutableLiveData<ClientExposedLoadedDetailTaskState> = MutableLiveData()

    val clientExposedLoadedDetailTaskState: LiveData<ClientExposedLoadedDetailTaskState> =
        _clientExposedLoadedDetailTaskState
    val clientExposedLoadedPredictionTaskState: LiveData<ClientExposedLoadedDetailTaskState> =
        _clientExposedLoadedPredictionTaskState

    lateinit var detailCompany: Company
    lateinit var predictionResponseDto: PredictionResponseDto

    init {
        val companyDao = (application as MainApplication).databaseReference.companyDao()
        val favouriteCompanyDao = application.databaseReference.favouriteCompanyDao()
        val networkService = application.networkService

        companyRepository = CompanyRepository(networkService, companyDao, favouriteCompanyDao)
        userToken = application.userToken
    }

    fun checkIfPropertiesAreInitialized(): Boolean {
        return this::detailCompany.isInitialized && this::predictionResponseDto.isInitialized
    }

    fun loadNewCompany(companyId: Long) = viewModelScope.launch {
        exposeGenericExposedTaskStateStarted(_clientExposedLoadedDetailTaskState)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                detailCompany = companyRepository.findCompanyById(companyId)

                exposeGenericExposedTaskStateSuccessful(
                    _clientExposedLoadedDetailTaskState,
                    "Successful detail company loaded"
                )
            } catch (exception: Exception) {
                logd(exception.message)

                exposeGenericExposedTaskStateError(
                    _clientExposedLoadedDetailTaskState,
                    exception.message
                )
            }
        }
    }

    fun requirePrediction(companyId: Long, predictionStartingDate: String) = viewModelScope.launch {
        exposeGenericExposedTaskStateStarted(_clientExposedLoadedPredictionTaskState)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                predictionResponseDto = companyRepository.requirePrediction(
                    companyId,
                    predictionStartingDate,
                    userToken
                )

                exposeGenericExposedTaskStateSuccessful(
                    _clientExposedLoadedPredictionTaskState,
                    "Successful prediction"
                )
            } catch (exception: Exception) {
                logd(exception.message)

                exposeGenericExposedTaskStateError(
                    _clientExposedLoadedPredictionTaskState,
                    exception.message
                )
            }
        }
    }


    private fun exposeGenericExposedTaskStateStarted(_clientExposedGenericLoadedTaskState: MutableLiveData<ClientExposedLoadedDetailTaskState>) {
        val newClientGenericExposedTaskState =
            ClientExposedLoadedDetailTaskState(
                isFinished = false,
                errorReceived = false,
                message = ""
            )
        _clientExposedGenericLoadedTaskState.postValue(newClientGenericExposedTaskState)
    }

    private fun exposeGenericExposedTaskStateSuccessful(
        _clientExposedGenericLoadedTaskState: MutableLiveData<ClientExposedLoadedDetailTaskState>,
        successMessage: String
    ) {
        val successClientGenericExposedTaskState =
            ClientExposedLoadedDetailTaskState(
                isFinished = true,
                errorReceived = false,
                message = successMessage
            )
        _clientExposedGenericLoadedTaskState.postValue(successClientGenericExposedTaskState)
    }

    private fun exposeGenericExposedTaskStateError(
        _clientExposedGenericLoadedTaskState: MutableLiveData<ClientExposedLoadedDetailTaskState>,
        errorMessage: String?
    ) {
        val errorClientGenericExposedTaskState =
            ClientExposedLoadedDetailTaskState(
                isFinished = true,
                errorReceived = true,
                message = errorMessage ?: "Unknown error"
            )
        _clientExposedGenericLoadedTaskState.postValue(errorClientGenericExposedTaskState)
    }
}