package ubb.cscluj.financialforecasting.admin.viewmodel.exposed_states

/*
    AdminExposedLoadedDetailTaskState for one-time request of querying some feedback message with ID X
    Used in AdminFeedbackDetailFragment
    Contains boolean: isInitialization - when nothing is needed to be done
    Contains state boolean: isCancelled
    Contains feedbackMessage: FeedbackMessage?
*/
data class AdminExposedLoadedDetailTaskState(

    val isFinished: Boolean = true,
    val errorReceived: Boolean = false,
    val message: String = ""
)