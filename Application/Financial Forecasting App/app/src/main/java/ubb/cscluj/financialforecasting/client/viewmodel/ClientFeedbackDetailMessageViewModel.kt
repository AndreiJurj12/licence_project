package ubb.cscluj.financialforecasting.client.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ubb.cscluj.financialforecasting.MainApplication
import ubb.cscluj.financialforecasting.client.viewmodel.exposed_states.ClientGenericExposedTaskState
import ubb.cscluj.financialforecasting.utils.logd
import ubb.cscluj.financialforecasting.model.FeedbackMessage
import ubb.cscluj.financialforecasting.repository.feedback_message.FeedbackMessageRepository

class ClientFeedbackDetailMessageViewModel(application: Application): AndroidViewModel(application) {
    private val feedbackMessageRepository: FeedbackMessageRepository
    private val _clientExposedLoadedDetailTaskState: MutableLiveData<ClientGenericExposedTaskState> = MutableLiveData()

    val clientExposedLoadedDetailTaskState: LiveData<ClientGenericExposedTaskState> = _clientExposedLoadedDetailTaskState
    lateinit var detailFeedbackMessage: FeedbackMessage


    init {
        val feedbackMessageDao = (application as MainApplication).databaseReference.feedbackMessageDao()
        val networkService = application.networkService
        feedbackMessageRepository =
            FeedbackMessageRepository(
                networkService,
                feedbackMessageDao
            )
    }

    fun loadNewFeedbackMessage(messageId: Long) {
        val newExposedLoadedDetailTaskState =
            ClientGenericExposedTaskState(
                isFinished = false,
                errorReceived = false,
                message = ""
            )
        _clientExposedLoadedDetailTaskState.postValue(newExposedLoadedDetailTaskState)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                detailFeedbackMessage = feedbackMessageRepository.findFeedbackMessageById(messageId)

                val successExposedLoadedDetailTaskState =
                    ClientGenericExposedTaskState(
                        isFinished = true,
                        errorReceived = false,
                        message = "Successful detail feedback message"
                    )
                _clientExposedLoadedDetailTaskState.postValue(successExposedLoadedDetailTaskState)
            } catch (exception: Exception) {
                logd(exception.message)


                val errorExposedLoadedDetailTaskState =
                    ClientGenericExposedTaskState(
                        isFinished = true,
                        errorReceived = true,
                        message = exception.message ?: "Unknown error"
                    )
                _clientExposedLoadedDetailTaskState.postValue(errorExposedLoadedDetailTaskState)
            }
        }
    }
}