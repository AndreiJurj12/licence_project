package ubb.cscluj.financialforecastingserver.core.model

import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "feedback_messages")
class FeedbackMessage(
        @Column(name = "message_request", nullable = false) var messageRequest: String,
        @Column(name = "creation_time", nullable = false) var creationTime: Instant,
        @Column(name = "message_response", nullable = false) var messageResponse: String = ""
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = User::class, optional = false)
    @JoinColumn(name = "user_id")
    var user: User? = null
}
