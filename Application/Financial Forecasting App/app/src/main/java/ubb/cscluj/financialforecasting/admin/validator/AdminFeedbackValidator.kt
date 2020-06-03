package ubb.cscluj.financialforecasting.admin.validator

import java.time.Year

class AdminFeedbackValidator {

    fun validateResponse(response: String): Boolean {
        return response.isNotBlank()
    }

}