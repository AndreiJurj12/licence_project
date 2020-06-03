package ubb.cscluj.financialforecasting.client.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ubb.cscluj.financialforecasting.MainApplication
import ubb.cscluj.financialforecasting.client.viewmodel.exposed_states.ClientGenericExposedTaskState
import ubb.cscluj.financialforecasting.utils.logd
import ubb.cscluj.financialforecasting.model.FeedbackMessage
import ubb.cscluj.financialforecasting.repository.feedback_message.FeedbackMessageRepository

class ClientFeedbackMessageViewModel(application: Application) : AndroidViewModel(application) {

    private val userToken: String
    private val userId: Long
    private val feedbackMessageRepository: FeedbackMessageRepository
    private val _clientGenericExposedTaskState: MutableLiveData<ClientGenericExposedTaskState> =
        MutableLiveData()

    val clientGenericExposedTaskState: LiveData<ClientGenericExposedTaskState> =
        _clientGenericExposedTaskState
    val clientFeedbackMessages: LiveData<List<FeedbackMessage>>

    init {
        val feedbackMessageDao =
            (application as MainApplication).databaseReference.feedbackMessageDao()
        val networkService = application.networkService

        userToken = application.userToken
        feedbackMessageRepository =
            FeedbackMessageRepository(
                networkService,
                feedbackMessageDao
            )
        userId = application.userId
        clientFeedbackMessages = feedbackMessageRepository.clientFeedbackMessages(userId)
    }

    fun initialLoading() = viewModelScope.launch {
        exposeGenericExposedTaskStateStarted()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                feedbackMessageRepository.initialLoadingUser(userToken, userId)
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
                feedbackMessageRepository.refreshFromServerUser(userToken, userId)
                exposeGenericExposedTaskStateSuccessful("Successful refresh")
            } catch (exception: Exception) {
                logd(exception.message)
                exposeGenericExposedTaskStateError(exception.message)
            }
        }
    }
    fun addNewUserFeedbackMessage(requestString: String) = viewModelScope.launch {
        exposeGenericExposedTaskStateStarted()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                feedbackMessageRepository.addNewUserFeedbackMessage(userToken, requestString)
                exposeGenericExposedTaskStateSuccessful("Successful addition")
            }
            catch (exception: Exception) {
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