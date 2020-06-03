package ubb.cscluj.financialforecasting.database_persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import ubb.cscluj.financialforecasting.model.FeedbackMessage

@Dao
interface FeedbackMessageDao {
    @Query("SELECT * FROM feedback_messages ORDER BY creation_time DESC")
    fun getAll(): LiveData<List<FeedbackMessage>>

    @Query("SELECT * FROM feedback_messages WHERE user_id = :userId ORDER BY creation_time DESC")
    fun getAllByUserId(userId: Long): LiveData<List<FeedbackMessage>>


    @Query("SELECT COUNT(*) FROM feedback_messages")
    suspend fun getCountFeedbackMessages(): Long

    @Query("SELECT COUNT(*) FROM feedback_messages WHERE user_id = :userId")
    fun getCountUserFeedbackMessages(userId: Long): Long

    @Query("SELECT * FROM feedback_messages WHERE id = :messageId")
    suspend fun findFeedbackMessageById(messageId: Long): FeedbackMessage

    @Query("DELETE FROM feedback_messages")
    suspend fun clearDatabaseTable()

    @Query("DELETE FROM feedback_messages WHERE user_id = :userId")
    suspend fun clearUserDatabaseTable(userId: Long)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedbackMessageList(feedbackMessageList: List<FeedbackMessage>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedbackMessage(feedbackMessage: FeedbackMessage)

    @Update
    suspend fun updateFeedbackMessage(feedbackMessage: FeedbackMessage)

}