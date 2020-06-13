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
import ubb.cscluj.financialforecasting.admin.viewmodel.exposed_states.AdminExposedLoadedDetailTaskState
import ubb.cscluj.financialforecasting.admin.viewmodel.exposed_states.AdminExposedTaskState
import ubb.cscluj.financialforecasting.utils.logd
import ubb.cscluj.financialforecasting.model.Company
import ubb.cscluj.financialforecasting.repository.company.CompanyRepository

class AdminCompanyUpdateDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val companyRepository: CompanyRepository
    private val _adminExposedLoadedDetailTaskState: MutableLiveData<AdminExposedLoadedDetailTaskState> = MutableLiveData()

    val adminExposedLoadedDetailTaskState: LiveData<AdminExposedLoadedDetailTaskState> = _adminExposedLoadedDetailTaskState
    lateinit var detailCompany: Company

    init {
        val companyDao = (application as MainApplication).databaseReference.companyDao()
        val favouriteCompanyDao = application.databaseReference.favouriteCompanyDao()
        val networkService = application.networkService
        companyRepository = CompanyRepository(networkService, companyDao, favouriteCompanyDao)
    }

    fun loadNewCompany(companyId: Long) = viewModelScope.launch{
        val newExposedLoadedDetailTaskState =
            AdminExposedLoadedDetailTaskState(
                isFinished = false,
                errorReceived = false,
                message = ""
            )
        _adminExposedLoadedDetailTaskState.postValue(newExposedLoadedDetailTaskState)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                detailCompany = companyRepository.findCompanyById(companyId)

                val successExposedLoadedDetailTaskState =
                    AdminExposedLoadedDetailTaskState(
                        isFinished = true,
                        errorReceived = false,
                        message = "Successful update detail company loaded"
                    )
                _adminExposedLoadedDetailTaskState.postValue(successExposedLoadedDetailTaskState)
            } catch (exception: Exception) {
                logd(exception.message)


                val errorExposedLoadedDetailTaskState =
                    AdminExposedLoadedDetailTaskState(
                        isFinished = true,
                        errorReceived = true,
                        message = exception.message ?: "Unknown error"
                    )
                _adminExposedLoadedDetailTaskState.postValue(errorExposedLoadedDetailTaskState)
            }
        }
    }
}