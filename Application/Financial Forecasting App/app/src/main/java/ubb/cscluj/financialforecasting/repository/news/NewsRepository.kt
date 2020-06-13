package ubb.cscluj.financialforecasting.repository.news

import ubb.cscluj.financialforecasting.model.news.News
import ubb.cscluj.financialforecasting.model.news.NewsResponse
import ubb.cscluj.financialforecasting.service.NetworkService
import ubb.cscluj.financialforecasting.utils.logd
import java.net.HttpURLConnection

class NewsRepository(
    private val networkService: NetworkService
) {


    suspend fun getRecentNews(): List<News> {
        val response = networkService.service.getRecentNews(apiKey = API_KEY)
        logd("Http response obtained: $response")
        if (response.raw().code != HttpURLConnection.HTTP_OK) {
            throw NewsNetworkException(
                "Get recent news failed!"
            )
        }

        val convertedResponse: NewsResponse = response.body() as NewsResponse
        return convertedResponse.articles
    }


    companion object Values {
        const val API_KEY = "1a4909df93fd4fa79ffd8115dc9d8a23"
    }
}