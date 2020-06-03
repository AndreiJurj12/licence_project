package ubb.cscluj.financialforecastingserver.web.mapper

import org.springframework.stereotype.Component
import ubb.cscluj.financialforecastingserver.core.model.FeedbackMessage
import ubb.cscluj.financialforecastingserver.web.dto.FeedbackMessageDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale


@Component
class FeedbackMessageDtoMapper : AbstractMapper<FeedbackMessage, FeedbackMessageDto>() {
    private val dateFormat = "dd-MM-yyyy HH:mm:ss"
    var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat)
            .withLocale(Locale.UK)
            .withZone(ZoneId.systemDefault())

    override fun convertDtoToModel(dto: FeedbackMessageDto): FeedbackMessage {
        //to not be used
        return FeedbackMessage(
                dto.messageRequest,
                Instant.MIN
        )
    }

    override fun convertModelToDto(model: FeedbackMessage): FeedbackMessageDto {
        return FeedbackMessageDto(
                messageId = model.id,
                messageRequest = model.messageRequest,
                creationTime = formatter.format(model.creationTime),
                messageResponse = model.messageResponse,
                userId = model.user?.id ?: -1
        )
    }

}
