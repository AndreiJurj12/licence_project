package ubb.cscluj.financialforecasting.admin.validator

import android.util.Patterns
import okhttp3.internal.toLongOrDefault
import java.time.Year

class AdminCompanyValidator {
    fun validateName(name: String): Boolean {
        return name.isNotBlank()
    }

    fun validateSymbol(symbol: String): Boolean {
        return symbol.isNotBlank()
    }

    fun validateDescription(description: String): Boolean {
        return description.isNotBlank()
    }

    fun validateFoundedYear(foundedYear: String): Boolean {
        val foundedYearLong = foundedYear.toLongOrDefault(0)
        return foundedYearLong >= 1800 && foundedYearLong <= Year.now().value
    }

    fun validateUrlLink(urlLink: String): Boolean {
        return urlLink.isNotBlank() && Patterns.WEB_URL.matcher(urlLink).matches()
    }

    fun validateUrlLogo(urlLogo: String): Boolean {
        return urlLogo.isNotBlank() && Patterns.WEB_URL.matcher(urlLogo).matches()
    }

    fun validateCsvDataPath(csvDataPath: String): Boolean {
        return csvDataPath.isNotBlank()
    }
}