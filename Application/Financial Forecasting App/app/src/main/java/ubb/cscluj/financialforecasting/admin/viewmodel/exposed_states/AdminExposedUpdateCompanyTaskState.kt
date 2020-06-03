package ubb.cscluj.financialforecasting.admin.viewmodel.exposed_states

import ubb.cscluj.financialforecasting.model.Company
import ubb.cscluj.financialforecasting.model.FeedbackMessage

/*
    AdminExposedAddCompanyTaskState for add rqeuest
    Contains boolean: isInitialization - when nothing is needed to be done
    Contains state boolean: isCancelled
    Contains company: Company?
*/
data class AdminExposedUpdateCompanyTaskState (
    var isInitialization: Boolean = false,
    var isCancelled: Boolean,
    var company: Company?
)