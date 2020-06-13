package ubb.cscluj.financialforecastingserver.core.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import ubb.cscluj.financialforecastingserver.core.exceptions.PredictionAPIFailedException
import ubb.cscluj.financialforecastingserver.web.dto.ExternalPredictionRequest
import ubb.cscluj.financialforecastingserver.web.dto.ExternalPredictionResponse


@Service
class PredictionServiceImpl : PredictionService {
    private var logging: Logger = LogManager.getLogger(PredictionServiceImpl::class.java)

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Autowired
    private lateinit var mapper: ObjectMapper

    override fun predict(externalPredictionRequest: ExternalPredictionRequest): ExternalPredictionResponse {
        val uriComponentsBuilder: UriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(predictionUrl)
        logging.debug("Uri request: ${uriComponentsBuilder.toUriString()}")

        val httpHeaders = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val request: HttpEntity<String> = HttpEntity(mapper.writeValueAsString(externalPredictionRequest), httpHeaders)

        val response = restTemplate.postForEntity(uriComponentsBuilder.toUriString(), request, ExternalPredictionResponse::class.java)
        if (response.statusCode != HttpStatus.OK || !response.hasBody()) {
            throw PredictionAPIFailedException("Failed prediction for symbol ${externalPredictionRequest.companyTickerSymbol}")
        }

        return response.body as ExternalPredictionResponse
    }

    companion object CustomFields {
        private const val baseHttpUrl: String = "http://127.0.0.1:5000"

        const val predictionUrl: String = "${baseHttpUrl}/prediction"
    }
}
