package ubb.cscluj.financialforecastingserver.web.dto

data class ExternalAPIResponseDto(
        val meta: ExternalAPIMetadataDto,
        val values: List<ExternalAPIOCHLVDto>,
        val status: String
)
