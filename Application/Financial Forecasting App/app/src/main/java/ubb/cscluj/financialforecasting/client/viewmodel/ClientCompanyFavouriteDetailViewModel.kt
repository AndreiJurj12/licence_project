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
import ubb.cscluj.financialforecasting.model.network_model.DateClosePrice
import ubb.cscluj.financialforecasting.repository.company.CompanyRepository
import ubb.cscluj.financialforecasting.utils.logd

class ClientCompanyFavouriteDetailViewModel(application: Application) :
    AndroidViewModel(application) {
    private val userToken: String
    private val companyRepository: CompanyRepository
    private val _clientExposedLoadedDetailTaskState: MutableLiveData<ClientExposedLoadedDetailTaskState> =
        MutableLiveData()
    private val _clientExposedLoadedHistoricDateClosePriceTaskState: MutableLiveData<ClientExposedLoadedDetailTaskState> =
        MutableLiveData()

    val clientExposedLoadedDetailTaskState: LiveData<ClientExposedLoadedDetailTaskState> =
        _clientExposedLoadedDetailTaskState
    val clientExposedLoadedHistoricDateClosePriceTaskState: LiveData<ClientExposedLoadedDetailTaskState> =
        _clientExposedLoadedHistoricDateClosePriceTaskState

    lateinit var detailCompany: Company
    var historicalCompanyDateClosePriceList: List<DateClosePrice> = emptyList()

    init {
        val companyDao = (application as MainApplication).databaseReference.companyDao()
        val favouriteCompanyDao = application.databaseReference.favouriteCompanyDao()
        val networkService = application.networkService
        userToken = application.userToken
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

    fun getHistoricalDataClosePrice(companyId: Long, requiredNoDays: Long) = viewModelScope.launch {
        val newExposedLoadedDetailTaskState =
            ClientExposedLoadedDetailTaskState(
                isFinished = false,
                errorReceived = false,
                message = ""
            )
        _clientExposedLoadedHistoricDateClosePriceTaskState.postValue(
            newExposedLoadedDetailTaskState
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                historicalCompanyDateClosePriceList = companyRepository.getHistoricalDataClosePrice(
                    companyId,
                    requiredNoDays,
                    userToken
                )

                val successExposedLoadedDetailTaskState =
                    ClientExposedLoadedDetailTaskState(
                        isFinished = true,
                        errorReceived = false,
                        message = "Successful load of new historical close prices"
                    )
                _clientExposedLoadedHistoricDateClosePriceTaskState.postValue(
                    successExposedLoadedDetailTaskState
                )
            } catch (exception: Exception) {
                logd(exception.message)


                val errorExposedLoadedDetailTaskState =
                    ClientExposedLoadedDetailTaskState(
                        isFinished = true,
                        errorReceived = true,
                        message = exception.message ?: "Unknown error"
                    )
                _clientExposedLoadedHistoricDateClosePriceTaskState.postValue(
                    errorExposedLoadedDetailTaskState
                )
            }
        }
    }

    fun resetHistoricalClosePrices() {
        historicalCompanyDateClosePriceList = emptyList()
        val successExposedLoadedDetailTaskState =
            ClientExposedLoadedDetailTaskState(
                isFinished = true,
                errorReceived = false,
                message = "Successful reset of historical close prices"
            )
        _clientExposedLoadedHistoricDateClosePriceTaskState.postValue(
            successExposedLoadedDetailTaskState
        )
    }
}