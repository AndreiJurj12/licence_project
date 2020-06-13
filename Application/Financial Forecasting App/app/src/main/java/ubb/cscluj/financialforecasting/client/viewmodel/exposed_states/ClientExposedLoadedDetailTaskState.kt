package ubb.cscluj.financialforecasting.client.viewmodel.exposed_states

data class ClientExposedLoadedDetailTaskState(
    val isFinished: Boolean = true,
    val errorReceived: Boolean = false,
    val message: String = ""
)