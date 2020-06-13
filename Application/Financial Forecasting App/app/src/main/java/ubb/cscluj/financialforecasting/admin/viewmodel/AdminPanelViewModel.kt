package ubb.cscluj.financialforecasting.admin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ubb.cscluj.financialforecasting.MainApplication
import ubb.cscluj.financialforecasting.admin.viewmodel.exposed_states.AdminExposedTaskState
import ubb.cscluj.financialforecasting.repository.company.CompanyRepository
import ubb.cscluj.financialforecasting.utils.logd

class AdminPanelViewModel (application: Application) : AndroidViewModel(application) {
    private val userToken: String
    private val companyRepository: CompanyRepository
    private val _adminExposedTaskState: MutableLiveData<AdminExposedTaskState> = MutableLiveData()

    val adminExposedTaskState: LiveData<AdminExposedTaskState> = _adminExposedTaskState

    init {
        val companyDao =
            (application as MainApplication).databaseReference.companyDao()
        val favouriteCompanyDao = application.databaseReference.favouriteCompanyDao()
        val networkService = application.networkService

        userToken = application.userToken
        companyRepository = CompanyRepository(networkService, companyDao, favouriteCompanyDao)
    }


    fun updateAllStockData() = viewModelScope.launch {
        exposeAdminExposedTaskStateStarted()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                companyRepository.updateAllStockData(userToken)
                exposeAdminExposedTaskStateSuccessful("Successful update of all stock data")
            } catch (exception: Exception) {
                logd(exception.message)
                exposeAdminExposedTaskStateError(exception.message)
            }
        }
    }

    private fun exposeAdminExposedTaskStateStarted() {
        val adminExposedTaskState =
            AdminExposedTaskState(
                isFinished = false,
                errorReceived = false,
                message = ""
            )
        _adminExposedTaskState.postValue(adminExposedTaskState)
    }

    private fun exposeAdminExposedTaskStateSuccessful(successMessage: String) {
        val successfulAdminExposedTaskState =
            AdminExposedTaskState(
                isFinished = true,
                errorReceived = false,
                message = successMessage
            )
        _adminExposedTaskState.postValue(successfulAdminExposedTaskState)
    }

    private fun exposeAdminExposedTaskStateError(errorMessage: String?) {
        val errorAdminExposedTaskState =
            AdminExposedTaskState(
                isFinished = true,
                errorReceived = true,
                message = errorMessage ?: "Unknown error"
            )
        _adminExposedTaskState.postValue(errorAdminExposedTaskState)
    }
}