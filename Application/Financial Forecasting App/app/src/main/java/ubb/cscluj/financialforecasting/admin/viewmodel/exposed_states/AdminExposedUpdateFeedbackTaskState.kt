package ubb.cscluj.financialforecasting.admin.viewmodel.exposed_states

import ubb.cscluj.financialforecasting.model.FeedbackMessage

/*
    AdminExposedUpdateTaskState for update request from AdminFeedbackDetailFragment to AdminFeedbackFragment
    Contains boolean: isInitialization - when nothing is needed to be done
    Contains state boolean: isCancelled
    Contains feedbackMessage: FeedbackMessage?
*/
data class AdminExposedUpdateFeedbackTaskState (
    var isInitialization: Boolean = false,
    var isCancelled: Boolean,
    var feedbackMessage: FeedbackMessage?
)