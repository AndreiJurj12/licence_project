package ubb.cscluj.financialforecasting.model.network_model

import java.util.*


data class FeedbackMessageDto(
    val messageId: Long,
    val messageRequest: String,
    val creationTime: Date,
    val messageResponse: String,
    val userId: Long)