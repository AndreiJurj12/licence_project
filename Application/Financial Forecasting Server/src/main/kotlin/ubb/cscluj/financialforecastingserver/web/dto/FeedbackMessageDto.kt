package ubb.cscluj.financialforecastingserver.web.dto

data class FeedbackMessageDto(
        val messageId: Long,
        val messageRequest: String,
        val creationTime: String,
        val messageResponse: String,
        val userId: Long)
