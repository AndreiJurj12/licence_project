package ubb.cscluj.financialforecasting.admin.viewmodel.exposed_states


/*
    AdminUsed for displaying generic loading message back and forth
    ExposedState for generic view model
    Contains state boolean: isFinished
    Contains state boolean: errorReceived
    Contains string: errorMessage
*/
data class AdminExposedTaskState(
    val isFinished: Boolean = true,
    val errorReceived: Boolean = false,
    val message: String = ""
)