package ubb.cscluj.financialforecastingserver.web.dto

data class ExternalAPIOCHLVDto(
        val datetime: String,
        val open: Double,
        val high: Double,
        val low: Double,
        val close: Double,
        val volume: Long
)

fun ExternalAPIOCHLVDto.convertToListOfStringsCsv(): List<String> {
    return listOf(datetime, open.toString(), high.toString(), low.toString(), close.toString(), volume.toString() )
}

