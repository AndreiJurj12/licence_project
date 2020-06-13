package ubb.cscluj.financialforecastingserver.core.service

import ubb.cscluj.financialforecastingserver.web.dto.ExternalPredictionRequest
import ubb.cscluj.financialforecastingserver.web.dto.ExternalPredictionResponse

interface PredictionService {
    fun predict(externalPredictionRequest: ExternalPredictionRequest): ExternalPredictionResponse
}
