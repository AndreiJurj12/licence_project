package ubb.cscluj.financialforecasting.admin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ubb.cscluj.financialforecasting.MainApplication
import ubb.cscluj.financialforecasting.admin.viewmodel.exposed_states.AdminExposedLoadedDetailTaskState
import ubb.cscluj.financialforecasting.repository.feedback_message.FeedbackMessageRepository
import ubb.cscluj.financialforecasting.utils.logd
import ubb.cscluj.financialforecasting.model.FeedbackMessage

class AdminFeedbackDetailMessageViewModel(application: Application): AndroidViewModel(application) {
    private val feedbackMessageRepository: FeedbackMessageRepository
    private val _adminExposedLoadedDetailTaskState: MutableLiveData<AdminExposedLoadedDetailTaskState> = MutableLiveData()

    val adminExposedLoadedDetailTaskState: LiveData<AdminExposedLoadedDetailTaskState> = _adminExposedLoadedDetailTaskState
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
            AdminExposedLoadedDetailTaskState(
                isFinished = false,
                errorReceived = false,
                message = ""
            )
        _adminExposedLoadedDetailTaskState.postValue(newExposedLoadedDetailTaskState)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                detailFeedbackMessage = feedbackMessageRepository.findFeedbackMessageById(messageId)

                val successExposedLoadedDetailTaskState =
                    AdminExposedLoadedDetailTaskState(
                        isFinished = true,
                        errorReceived = false,
                        message = "Successful detail feedback message"
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