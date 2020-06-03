package ubb.cscluj.financialforecastingserver.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ubb.cscluj.financialforecastingserver.core.model.FeedbackMessage
import ubb.cscluj.financialforecastingserver.core.model.User

@Repository
interface FeedbackMessageRepository : JpaRepository<FeedbackMessage, Long> {
    @Query("SELECT fm from FeedbackMessage fm WHERE fm.user = :user")
    fun getFeedbackMessageByUser(@Param("user") user: User): List<FeedbackMessage>
}
