package ubb.cscluj.financialforecastingserver.core.service

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import ubb.cscluj.financialforecastingserver.core.exceptions.ExternalAPIFailedException
import ubb.cscluj.financialforecastingserver.web.dto.ExternalAPIResponseDto
import ubb.cscluj.financialforecastingserver.web.dto.convertToListOfStringsCsv
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Service
class ExternalStockDataServiceImpl : ExternalStockDataService {
    private var logging: Logger = LogManager.getLogger(ExternalStockDataServiceImpl::class.java)

    @Value("\${twelvedata.api-key}")
    private lateinit var apikey: String

    @Autowired
    private lateinit var restTemplate: RestTemplate

    override fun loadInitialData(stockTickerSymbol: String, csvDataPath: String) {
        logging.debug("Entered loadInitialData() with stockTickerSymbol=$stockTickerSymbol and csvDataPath=$csvDataPath")
        //logging.debug("apikey for twelve data is: $apikey")

        val externalAPIResponseDto = requestDataFromStartingDate(initialStartDateTrial, stockTickerSymbol)
        val csvData: MutableList<List<String>> = mutableListOf()
        csvData.add(headersList)
        externalAPIResponseDto.values.sortedBy { dateTimeStrToLocalDate(it.datetime) }.forEach {
            csvData.add(it.convertToListOfStringsCsv())
        }
        csvWriter().writeAll(csvData, csvDataPath)
    }

    override fun updateCompanyLatestData(stockTickerSymbol: String, csvDataPath: String) {
        var latestDate = initialStartDateTrial.minusDays(1)

        //get the latest date for our data
        csvReader().open(csvDataPath) {
            readAllWithHeaderAsSequence().forEach { row ->
                val currentDateString = row[dateHeaderLabel]
                if (currentDateString != null) {
                    val currentDate = LocalDate.parse(currentDateString, dateFormatter)
                    if (currentDate > latestDate)
                        latestDate = currentDate
                }
            }
        }


        // our new start date for our request will be latestDate + 1 day
        val newStartingDateRequest = latestDate.plusDays(1)
        val currentDate = LocalDate.now()


        if (newStartingDateRequest < currentDate) {
            val externalAPIResponseDto = requestDataFromStartingDate(newStartingDateRequest, stockTickerSymbol)
            logging.debug("External api request was successful - appending to file")
            val csvData: MutableList<List<String>> = mutableListOf()
            externalAPIResponseDto.values.sortedBy { dateTimeStrToLocalDate(it.datetime) }.forEach {
                csvData.add(it.convertToListOfStringsCsv())
            }
            csvWriter().writeAll(csvData, csvDataPath, append = true)
        }
    }

    @Throws(ExternalAPIFailedException::class)
    private fun requestDataFromStartingDate(startingDate: LocalDate, stockTickerSymbol: String): ExternalAPIResponseDto {
        val uriComponentsBuilder: UriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(baseHttpUrl)
                .queryParam(intervalLabel, interval)
                .queryParam(apikeyLabel, apikey)
                .queryParam(symbolLabel, stockTickerSymbol)
                .queryParam(startDateLabel, startingDate.format(dateFormatter))
        logging.debug("Uri request: ${uriComponentsBuilder.toUriString()}")

        val response: ResponseEntity<ExternalAPIResponseDto> = restTemplate.getForEntity(uriComponentsBuilder.toUriString(), ExternalAPIResponseDto::class.java)
        if (response.statusCode != HttpStatus.OK || !response.hasBody()) {
            throw ExternalAPIFailedException("Initial loading of data failed for symbol $stockTickerSymbol")
        }

        return response.body as ExternalAPIResponseDto
    }


    companion object CustomFields {
        const val baseHttpUrl: String = "https://api.twelvedata.com/time_series"
        const val intervalLabel: String = "interval"
        const val apikeyLabel: String = "apikey"
        const val symbolLabel: String = "symbol"
        const val startDateLabel: String = "start_date"
        const val outputSizeLabel: String = "outputsize"

        const val interval: String = "1day"
        val initialStartDateTrial: LocalDate = LocalDate.of(2000, 1, 1)
        const val initialOutputSize: Int = 5000


        private const val dateFormat = "yyyy-MM-dd"
        var dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat)

        const val dateHeaderLabel: String = "Date"
        var headersList: List<String> = listOf("Date", "Open", "High", "Low", "Close", "Volume")
        val dateTimeStrToLocalDate: (String) -> LocalDate = {
            LocalDate.parse(it, dateFormatter)
        }
    }


}
