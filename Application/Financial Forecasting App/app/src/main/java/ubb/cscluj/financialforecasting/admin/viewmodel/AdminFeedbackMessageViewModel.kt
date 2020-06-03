package ubb.cscluj.financialforecasting.admin.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ubb.cscluj.financialforecasting.MainApplication
import ubb.cscluj.financialforecasting.admin.viewmodel.exposed_states.AdminExposedTaskState
import ubb.cscluj.financialforecasting.repository.feedback_message.FeedbackMessageRepository
import ubb.cscluj.financialforecasting.utils.logd
import ubb.cscluj.financialforecasting.model.FeedbackMessage

class AdminFeedbackMessageViewModel(application: Application) : AndroidViewModel(application) {

    private val userToken: String
    private val feedbackMessageRepository: FeedbackMessageRepository
    private val _adminExposedTaskState: MutableLiveData<AdminExposedTaskState> = MutableLiveData()

    val adminExposedTaskState: LiveData<AdminExposedTaskState> = _adminExposedTaskState
    val allFeedbackMessages: LiveData<List<FeedbackMessage>>

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
        allFeedbackMessages = feedbackMessageRepository.allFeedbackMessages
    }


    fun initialLoading() = viewModelScope.launch {
        val newExposedTaskState =
            AdminExposedTaskState(
                isFinished = false,
                errorReceived = false,
                message = ""
            )
        _adminExposedTaskState.postValue(newExposedTaskState)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                feedbackMessageRepository.initialLoading(userToken)

                val successExposedTaskState =
                    AdminExposedTaskState(
                        isFinished = true,
                        errorReceived = false,
                        message = "Successful initial loading"
                    )
                _adminExposedTaskState.postValue(successExposedTaskState)
            } catch (exception: Exception) {
                logd(exception.message)


                val errorExposedTaskState =
                    AdminExposedTaskState(
                        isFinished = true,
                        errorReceived = true,
                        message = exception.message ?: "Unknown error"
                    )
                _adminExposedTaskState.postValue(errorExposedTaskState)
            }
        }
    }


    fun refreshFromServer() = viewModelScope.launch {
        val newExposedTaskState =
            AdminExposedTaskState(
                isFinished = false,
                errorReceived = false,
                message = ""
            )
        _adminExposedTaskState.postValue(newExposedTaskState)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                feedbackMessageRepository.refreshFromServer(userToken)

                val successExposedTaskState =
                    AdminExposedTaskState(
                        isFinished = true,
                        errorReceived = false,
                        message = "Successful refresh"
                    )
                _adminExposedTaskState.postValue(successExposedTaskState)
            } catch (exception: Exception) {
                logd(exception.message)


                val errorExposedTaskState =
                    AdminExposedTaskState(
                        isFinished = true,
                        errorReceived = true,
                        message = exception.message ?: "Unknown error"
                    )
                _adminExposedTaskState.postValue(errorExposedTaskState)
            }
        }
    }

    fun updateFeedbackMessageResponse(feedbackMessage: FeedbackMessage) = viewModelScope.launch {
        val newExposedTaskState =
            AdminExposedTaskState(
                isFinished = false,
                errorReceived = false,
                message = ""
            )
        _adminExposedTaskState.postValue(newExposedTaskState)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                feedbackMessageRepository.updateFeedbackMessageResponseWithServer(
                    userToken = userToken,
                    feedbackMessage = feedbackMessage
                )

                val successExposedTaskState =
                    AdminExposedTaskState(
                        isFinished = true,
                        errorReceived = false,
                        message = "Successful update feedback message response"
                    )
                _adminExposedTaskState.postValue(successExposedTaskState)
            } catch (exception: Exception) {
                logd(exception.message)


                val errorExposedTaskState =
                    AdminExposedTaskState(
                        isFinished = true,
                        errorReceived = true,
                        message = exception.message ?: "Unknown error"
                    )
                _adminExposedTaskState.postValue(errorExposedTaskState)
            }
        }
    }
}