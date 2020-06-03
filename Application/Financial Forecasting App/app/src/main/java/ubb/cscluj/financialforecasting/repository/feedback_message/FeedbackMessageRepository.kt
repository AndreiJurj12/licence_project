package ubb.cscluj.financialforecasting.repository.feedback_message

import androidx.lifecycle.LiveData
import ubb.cscluj.financialforecasting.database_persistence.FeedbackMessageDao
import ubb.cscluj.financialforecasting.utils.logd
import ubb.cscluj.financialforecasting.model.FeedbackMessage
import ubb.cscluj.financialforecasting.model.network_model.FeedbackMessageDto
import ubb.cscluj.financialforecasting.model.network_model.FeedbackMessageRequestDto
import ubb.cscluj.financialforecasting.model.network_model.UpdateFeedbackMessageResponseDto
import ubb.cscluj.financialforecasting.service.NetworkService
import java.net.HttpURLConnection

class FeedbackMessageRepository(
    private val networkService: NetworkService,
    private val feedbackMessageDao: FeedbackMessageDao
) {
    val allFeedbackMessages: LiveData<List<FeedbackMessage>> = feedbackMessageDao.getAll()

    fun clientFeedbackMessages(userId: Long): LiveData<List<FeedbackMessage>> {
        return feedbackMessageDao.getAllByUserId(userId)
    }

    suspend fun refreshFromServer(userToken: String) {
        logd("Enter FeedbackMessageRepository: refreshFromServer()")
        val response = networkService.service.getAllFeedbackMessages(userToken)
        logd("Http response obtained: $response")
        if (response.raw().code != HttpURLConnection.HTTP_OK) {
            throw FeedbackMessageNetworkException(
                "Refresh from server failed!"
            )
        }

        val newFeedbackMessages = response.body() as List<FeedbackMessageDto>
        val convertedFeedbackMessages = newFeedbackMessages.map {
            FeedbackMessage(
                messageRequest = it.messageRequest,
                creationTime = it.creationTime,
                messageResponse = it.messageResponse,
                userId = it.userId,
                id = it.messageId
            )
        }
        logd("NewFeedbackMessages obtained: $convertedFeedbackMessages")

        feedbackMessageDao.clearDatabaseTable()
        feedbackMessageDao.insertFeedbackMessageList(convertedFeedbackMessages)
    }

    suspend fun initialLoading(userToken: String) {
        val countFeedbackMessages = feedbackMessageDao.getCountFeedbackMessages()

        if (countFeedbackMessages == 0L) {
            refreshFromServer(userToken)
        }
    }


    suspend fun findFeedbackMessageById(messageId: Long): FeedbackMessage {
        return feedbackMessageDao.findFeedbackMessageById(messageId)
    }

    suspend fun updateFeedbackMessageResponseWithServer(
        userToken: String,
        feedbackMessage: FeedbackMessage
    ) {
        logd("updateFeedbackMessageResponseWithServer() Enter")
        val updateFeedbackMessageResponseDto = UpdateFeedbackMessageResponseDto(
            messageResponse = feedbackMessage.messageResponse,
            feedbackMessageId = feedbackMessage.id
        )

        val response = networkService.service.updateFeedbackMessageResponse(
            userToken,
            updateFeedbackMessageResponseDto
        )
        if (response.raw().code != HttpURLConnection.HTTP_OK) {
            throw FeedbackMessageNetworkException(
                "Update feedback response message failed!"
            )
        }

        val responseFeedbackMessageDto = response.body() as FeedbackMessageDto

        val updatedFeedbackMessage = FeedbackMessage(
            id = responseFeedbackMessageDto.messageId,
            messageRequest = responseFeedbackMessageDto.messageRequest,
            messageResponse = responseFeedbackMessageDto.messageResponse,
            creationTime = responseFeedbackMessageDto.creationTime,
            userId = responseFeedbackMessageDto.userId
        )
        feedbackMessageDao.updateFeedbackMessage(updatedFeedbackMessage)
        logd("updateFeedbackMessageResponseWithServer() Successful")
    }

    suspend fun initialLoadingUser(userToken: String, userId: Long) {
        val countFeedbackMessages = feedbackMessageDao.getCountUserFeedbackMessages(userId)

        if (countFeedbackMessages == 0L) {
            refreshFromServerUser(userToken, userId)
        }
    }

    suspend fun refreshFromServerUser(userToken: String, userId: Long) {
        logd("Enter FeedbackMessageRepository: refreshFromServerUser()")
        val response = networkService.service.getAllUserFeedbackMessages(userToken)
        logd("Http response obtained: $response")
        if (response.raw().code != HttpURLConnection.HTTP_OK) {
            throw FeedbackMessageNetworkException(
                "Refresh from server failed!"
            )
        }

        val newFeedbackMessages = response.body() as List<FeedbackMessageDto>
        val convertedFeedbackMessages = newFeedbackMessages.map {
            FeedbackMessage(
                messageRequest = it.messageRequest,
                creationTime = it.creationTime,
                messageResponse = it.messageResponse,
                userId = it.userId,
                id = it.messageId
            )
        }
        logd("NewFeedbackMessages obtained: $convertedFeedbackMessages")

        feedbackMessageDao.clearUserDatabaseTable(userId)
        feedbackMessageDao.insertFeedbackMessageList(convertedFeedbackMessages)
    }

    suspend fun addNewUserFeedbackMessage(userToken: String, requestString: String) {
        logd("Enter FeedbackMessageRepository: addNewUserFeedbackMessage()")

        val response = networkService.service.saveNewUserFeedbackMessage(
            userToken,
            FeedbackMessageRequestDto(
                messageRequest = requestString
            )
        )
        if (response.raw().code != HttpURLConnection.HTTP_OK) {
            throw FeedbackMessageNetworkException(
                "Save new user feedback message failed!"
            )
        }
        val responseFeedbackMessageDto = response.body() as FeedbackMessageDto

        val savedFeedbackMessage = FeedbackMessage(
            id = responseFeedbackMessageDto.messageId,
            messageRequest = responseFeedbackMessageDto.messageRequest,
            messageResponse = responseFeedbackMessageDto.messageResponse,
            creationTime = responseFeedbackMessageDto.creationTime,
            userId = responseFeedbackMessageDto.userId
        )
        feedbackMessageDao.insertFeedbackMessage(savedFeedbackMessage)
        logd("addNewUserFeedbackMessage() Successful")

    }
}