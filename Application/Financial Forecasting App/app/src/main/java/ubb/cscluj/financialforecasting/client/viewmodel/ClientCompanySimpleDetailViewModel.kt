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
import ubb.cscluj.financialforecasting.repository.company.CompanyRepository
import ubb.cscluj.financialforecasting.utils.logd

class ClientCompanySimpleDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val companyRepository: CompanyRepository
    private val _clientExposedLoadedDetailTaskState: MutableLiveData<ClientExposedLoadedDetailTaskState> = MutableLiveData()

    val clientExposedLoadedDetailTaskState: LiveData<ClientExposedLoadedDetailTaskState> = _clientExposedLoadedDetailTaskState

    lateinit var detailCompany: Company

    init {
        val companyDao = (application as MainApplication).databaseReference.companyDao()
        val favouriteCompanyDao = application.databaseReference.favouriteCompanyDao()
        val networkService = application.networkService
        companyRepository = CompanyRepository(networkService, companyDao, favouriteCompanyDao)
    }

    fun checkIfPropertiesAreInitialized(): Boolean {
        return this::detailCompany.isInitialized
    }

    fun loadNewCompany(companyId: Long) = viewModelScope.launch {
        val newExposedLoadedDetailTaskState =
            ClientExposedLoadedDetailTaskState(
                isFinished = false,
                errorReceived = false,
                message = ""
            )
        _clientExposedLoadedDetailTaskState.postValue(newExposedLoadedDetailTaskState)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                detailCompany = companyRepository.findCompanyById(companyId)

                val successExposedLoadedDetailTaskState =
                    ClientExposedLoadedDetailTaskState(
                        isFinished = true,
                        errorReceived = false,
                        message = "Successful detail company loaded"
                    )
                _clientExposedLoadedDetailTaskState.postValue(successExposedLoadedDetailTaskState)
            } catch (exception: Exception) {
                logd(exception.message)


                val errorExposedLoadedDetailTaskState =
                    ClientExposedLoadedDetailTaskState(
                        isFinished = true,
                        errorReceived = true,
                        message = exception.message ?: "Unknown error"
                    )
                _clientExposedLoadedDetailTaskState.postValue(errorExposedLoadedDetailTaskState)
            }
        }
    }
}