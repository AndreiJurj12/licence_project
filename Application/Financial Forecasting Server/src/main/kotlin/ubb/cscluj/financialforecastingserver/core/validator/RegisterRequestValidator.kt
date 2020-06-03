package ubb.cscluj.financialforecastingserver.core.validator

import org.springframework.stereotype.Component
import ubb.cscluj.financialforecastingserver.web.dto.RegisterRequestDto

@Component
class RegisterRequestValidator : Validator<RegisterRequestDto> {

    override fun validate(entity: RegisterRequestDto) {
        if (entity.email.isBlank() || !emailRegex.matches(entity.email)) {
            throw ValidationException(invalidEmail)
        }

        if (entity.password.isBlank() || entity.password.length < 8) {
            throw ValidationException(invalidPassword)
        }
    }

    companion object ErrorMessages {
        private const val EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$"
        val emailRegex: Regex = EMAIL_REGEX.toRegex()

        const val invalidEmail: String = "Invalid email field"
        const val invalidPassword: String = "Invalid password field"
    }
}
