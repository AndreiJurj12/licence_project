package ubb.cscluj.financialforecasting.login.validator

import android.util.Patterns

class RegisterValidator {
    fun validateEmail(email: String): Boolean {
        if (email.isEmpty()) {
            return false
        }
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validatePassword(password: String): Boolean {
        if (password.isEmpty()) {
            return false
        }
        return (password.length >= 8)
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): Boolean {
        if (confirmPassword.isEmpty() || password != confirmPassword) {
            return false
        }
        return (confirmPassword.length >= 8)
    }
}