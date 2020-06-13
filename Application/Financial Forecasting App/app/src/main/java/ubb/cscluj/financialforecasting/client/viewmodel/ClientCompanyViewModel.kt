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
import ubb.cscluj.financialforecasting.client.viewmodel.exposed_states.ClientGenericExposedTaskState
import ubb.cscluj.financialforecasting.model.Company
import ubb.cscluj.financialforecasting.model.FavouriteCompany
import ubb.cscluj.financialforecasting.repository.company.CompanyRepository
import ubb.cscluj.financialforecasting.utils.logd

class ClientCompanyViewModel (application: Application) : AndroidViewModel(application) {
    private val userToken: String
    private val companyRepository: CompanyRepository
    private val _clientGenericExposedTaskState: MutableLiveData<ClientGenericExposedTaskState> = MutableLiveData()

    val clientGenericExposedTaskState: LiveData<ClientGenericExposedTaskState> = _clientGenericExposedTaskState
    val allCompanies: LiveData<List<Company>>
    val allFavouriteCompanies: LiveData<List<FavouriteCompany>>

    init {
        val companyDao =
            (application as MainApplication).databaseReference.companyDao()
        val favouriteCompanyDao =
            application.databaseReference.favouriteCompanyDao()
        val networkService = application.networkService

        userToken = application.userToken
        companyRepository =
            CompanyRepository(
                networkService,
                companyDao,
                favouriteCompanyDao
            )
        allCompanies = companyRepository.allCompanies
        allFavouriteCompanies = companyRepository.allFavouriteCompanies
    }

    fun initialLoading() = viewModelScope.launch {
        exposeGenericExposedTaskStateStarted()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                companyRepository.initialLoading(userToken)
                companyRepository.initialLoadingFavourite(userToken)
                exposeGenericExposedTaskStateSuccessful("Successful initial loading")
            } catch (exception: Exception) {
                logd(exception.message)
                exposeGenericExposedTaskStateError(exception.message)
            }
        }
    }

    fun refreshFromServer() = viewModelScope.launch {
        exposeGenericExposedTaskStateStarted()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                companyRepository.refreshFromServer(userToken)
                companyRepository.initialLoadingFavourite(userToken)
                exposeGenericExposedTaskStateSuccessful("Successful refresh")
            } catch (exception: Exception) {
                logd(exception.message)
                exposeGenericExposedTaskStateError(exception.message)
            }
        }
    }

    fun switchFavouriteCompany(company: Company) = viewModelScope.launch {
        exposeGenericExposedTaskStateStarted()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                companyRepository.switchFavouriteCompany(company, userToken)
                exposeGenericExposedTaskStateSuccessful("Successful update of favourite usage")
            } catch (exception: Exception) {
                logd(exception.message)
                exposeGenericExposedTaskStateError(exception.message)
            }
        }
    }

    private fun exposeGenericExposedTaskStateStarted() {
        val newClientGenericExposedTaskState =
            ClientGenericExposedTaskState(
                isFinished = false,
                errorReceived = false,
                message = ""
            )
        _clientGenericExposedTaskState.postValue(newClientGenericExposedTaskState)
    }

    private fun exposeGenericExposedTaskStateSuccessful(successMessage: String) {
        val successClientGenericExposedTaskState =
            ClientGenericExposedTaskState(
                isFinished = true,
                errorReceived = false,
                message = successMessage
            )
        _clientGenericExposedTaskState.postValue(successClientGenericExposedTaskState)
    }

    private fun exposeGenericExposedTaskStateError(errorMessage: String?) {
        val errorClientGenericExposedTaskState =
            ClientGenericExposedTaskState(
                isFinished = true,
                errorReceived = true,
                message = errorMessage ?: "Unknown error"
            )
        _clientGenericExposedTaskState.postValue(errorClientGenericExposedTaskState)
    }
}