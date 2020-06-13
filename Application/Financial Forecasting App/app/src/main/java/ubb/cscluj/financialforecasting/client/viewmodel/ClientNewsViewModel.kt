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
import ubb.cscluj.financialforecasting.model.news.News
import ubb.cscluj.financialforecasting.repository.news.NewsRepository
import ubb.cscluj.financialforecasting.utils.logd

class ClientNewsViewModel(application: Application) : AndroidViewModel(application) {
    private val userId: Long
    private val newsRepository: NewsRepository
    private val _clientGenericExposedTaskState: MutableLiveData<ClientGenericExposedTaskState> =
        MutableLiveData()

    val clientGenericExposedTaskState: LiveData<ClientGenericExposedTaskState> =
        _clientGenericExposedTaskState
    var news: List<News> = emptyList()

    init {
        val networkService = (application as MainApplication).networkService

        newsRepository = NewsRepository(networkService)
        userId = application.userId
    }

    fun refreshFromServer() = viewModelScope.launch {
        exposeGenericExposedTaskStateStarted()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                news = newsRepository.getRecentNews()
                exposeGenericExposedTaskStateSuccessful("Successful refresh")
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