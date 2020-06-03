package ubb.cscluj.financialforecasting.client.validator

class ClientFeedbackMessageValidator {
    fun validateRequest(request: String): Boolean {
        return request.isNotBlank()
    }
}