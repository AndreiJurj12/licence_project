package ubb.cscluj.financialforecastingserver.core.repository

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import ubb.cscluj.financialforecastingserver.core.model.FeedbackMessage
import ubb.cscluj.financialforecastingserver.core.model.User
import java.time.Instant

@DataJpaTest
class FeedbackMessageRepositoryTest {
    @Autowired
    lateinit var entityManager: TestEntityManager
    @Autowired
    lateinit var feedbackMessageRepository: FeedbackMessageRepository

    @Test
    fun `When feedback messages exist for some user, retrieve them all`() {
        val user = User(email = "test@gmail.com",
                password = "12345678",
                isAdmin = false)
        val persistedUser = entityManager.persist(user)

        val feedbackMessageOne = FeedbackMessage(
                messageRequest = "Request 1",
                creationTime = Instant.now(),
                messageResponse = "Response 1"
        )
        val feedbackMessageTwo = FeedbackMessage(
                messageRequest = "Request 2",
                creationTime = Instant.now(),
                messageResponse = ""
        )

        feedbackMessageOne.user = persistedUser
        entityManager.persist(feedbackMessageOne)
        feedbackMessageTwo.user = persistedUser
        entityManager.persist(feedbackMessageTwo)


        val userFeedbackMessageList = feedbackMessageRepository.getFeedbackMessageByUser(persistedUser)
        assert(userFeedbackMessageList.size == 2)
        assert(userFeedbackMessageList.contains(feedbackMessageOne))
        assert(userFeedbackMessageList.contains(feedbackMessageTwo))
    }

    @Test
    fun `When feedback messages don't for some user, retrieve zero`() {
        val user = User(email = "test@gmail.com",
                password = "12345678",
                isAdmin = false)
        val persistedUser = entityManager.persist(user)

        val userFeedbackMessageList = feedbackMessageRepository.getFeedbackMessageByUser(persistedUser)
        assert(userFeedbackMessageList.isEmpty())
    }
}
