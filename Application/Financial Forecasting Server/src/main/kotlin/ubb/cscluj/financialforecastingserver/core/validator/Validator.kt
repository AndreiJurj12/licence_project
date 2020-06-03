package ubb.cscluj.financialforecastingserver.core.validator

interface Validator<T> {
    @Throws(ValidationException::class)
    fun validate(entity: T)
}
