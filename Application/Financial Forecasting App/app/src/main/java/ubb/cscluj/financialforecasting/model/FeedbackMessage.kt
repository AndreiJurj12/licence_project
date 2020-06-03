package ubb.cscluj.financialforecasting.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "feedback_messages")
data class FeedbackMessage(
    @PrimaryKey(autoGenerate = true)  @ColumnInfo(name = "id") var id: Long = 0,
    @ColumnInfo(name = "message_request") var messageRequest: String,
    @ColumnInfo(name = "creation_time") var creationTime: Date,
    @ColumnInfo(name = "message_response") var messageResponse: String,
    @ColumnInfo(name = "user_id") var userId: Long = -1
)