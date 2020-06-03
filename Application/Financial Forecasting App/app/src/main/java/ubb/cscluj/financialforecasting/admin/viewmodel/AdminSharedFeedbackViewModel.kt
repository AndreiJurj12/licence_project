package ubb.cscluj.financialforecasting.admin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ubb.cscluj.financialforecasting.admin.viewmodel.exposed_states.AdminExposedUpdateFeedbackTaskState
import ubb.cscluj.financialforecasting.model.FeedbackMessage

class AdminSharedFeedbackViewModel (application: Application) : AndroidViewModel(application) {
    private val _adminExposedUpdateFeedbackTaskState: MutableLiveData<AdminExposedUpdateFeedbackTaskState> = MutableLiveData()

    val adminExposedUpdateFeedbackTaskState: LiveData<AdminExposedUpdateFeedbackTaskState> = _adminExposedUpdateFeedbackTaskState


    fun addNewUpdateTask(feedbackMessage: FeedbackMessage) {
        _adminExposedUpdateFeedbackTaskState.value =
            AdminExposedUpdateFeedbackTaskState(
                isCancelled = false,
                feedbackMessage = feedbackMessage
            )
    }

    fun addCancelledUpdateTask() {
        _adminExposedUpdateFeedbackTaskState.value =
            AdminExposedUpdateFeedbackTaskState(
                isCancelled = true,
                feedbackMessage = null
            )
    }

    fun revertUninitializedUpdateTask() {
        _adminExposedUpdateFeedbackTaskState.value =
            AdminExposedUpdateFeedbackTaskState(
                isInitialization = true,
                isCancelled = false,
                feedbackMessage = null
            )
    }
}