package ubb.cscluj.financialforecasting.service

import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ubb.cscluj.financialforecasting.model.User
import ubb.cscluj.financialforecasting.model.network_model.*
import ubb.cscluj.financialforecasting.model.news.News
import ubb.cscluj.financialforecasting.model.news.NewsResponse
import java.util.concurrent.TimeUnit

class NetworkService(private var context: Context) {
    private val URL = "http://192.168.43.214:8080"

    //private val URL = "http://10.0.2.2:8080"
    private var cacheSize = 10 * 1024 * 1024 // 10 MiB
    private val dateFormatString: String = "dd-MM-yyyy HH:mm:ss"

    private val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        this.level = HttpLoggingInterceptor.Level.BODY
    }

    private var gson = GsonBuilder()
        .setLenient()
        .setDateFormat(dateFormatString)
        .create()

    private lateinit var cache: Cache
    lateinit var client: OkHttpClient
    private lateinit var retrofit: Retrofit
    lateinit var service: Service


    companion object {
        @Volatile
        private var instance: NetworkService? = null

        fun getInstance(context: Context): NetworkService {
            return instance ?: synchronized(this) {
                return NetworkService(context).also { networkService ->
                    run {
                        networkService.cache =
                            Cache(context.cacheDir, networkService.cacheSize.toLong())

                        networkService.client = OkHttpClient.Builder().apply {
                            this.addInterceptor(networkService.interceptor)
                        }
                            .cache(networkService.cache)
                            .readTimeout(1, TimeUnit.MINUTES)
                            .writeTimeout(1, TimeUnit.MINUTES)
                            .build()

                        networkService.retrofit = Retrofit.Builder()
                            .baseUrl(networkService.URL)
                            .addConverterFactory(GsonConverterFactory.create(networkService.gson))
                            .client(networkService.client)
                            .build()

                        networkService.service = networkService.retrofit.create(Service::class.java)
                    }
                }
            }
        }
    }


    interface Service {

        @POST("/api/login")
        suspend fun login(@Body user: User): Response<LoginResponse>

        @POST("/api/register")
        suspend fun register(@Body registerRequest: RegisterRequest): Response<RegisterResponse>

        @POST("/api/user/logout")
        suspend fun logout(
            @Header("Authorization") userToken: String,
            @Body logoutRequest: LogoutRequest
        ): Response<LogoutResponse>

        @GET("/api/feedback/admin/all_feedback_messages")
        suspend fun getAllFeedbackMessages(
            @Header("Authorization") userToken: String
        ): Response<List<FeedbackMessageDto>>

        @PUT("/api/feedback/admin/update_response_feedback_message")
        suspend fun updateFeedbackMessageResponse(
            @Header("Authorization") userToken: String,
            @Body updateFeedbackMessageResponseDto: UpdateFeedbackMessageResponseDto
        ): Response<FeedbackMessageDto>

        @GET("/api/feedback/all_user_feedback_messages")
        suspend fun getAllUserFeedbackMessages(
            @Header("Authorization") userToken: String
        ): Response<List<FeedbackMessageDto>>

        @POST("/api/feedback/save_new_user_feedback_message")
        suspend fun saveNewUserFeedbackMessage(
            @Header("Authorization") userToken: String,
            @Body feedbackMessageRequestDto: FeedbackMessageRequestDto
        ): Response<FeedbackMessageDto>

        @GET("/api/company/get_all_companies")
        suspend fun getAllCompanies(
            @Header("Authorization") userToken: String
        ): Response<List<CompanyDto>>

        @POST("/api/company/admin/save_new_company")
        suspend fun saveNewCompany(
            @Header("Authorization") userToken: String,
            @Body companyAdditionRequestDto: CompanyAdditionRequestDto
        ): Response<CompanyDto>

        @PUT("/api/company/admin/update_company_details")
        suspend fun updateCompany(
            @Header("Authorization") userToken: String,
            @Body companyUpdateRequestDto: CompanyUpdateRequestDto): Response<CompanyDto>

        @POST("/api/company/admin/update_all_stock_data")
        suspend fun updateAllStockData(
            @Header("Authorization") userToken: String
        ): Response<UpdateAllStockDataResponseDto>

        @GET("/api/company/get_favourite_user_companies")
        suspend fun getAllFavouriteCompanies(
            @Header("Authorization") userToken: String
        ): Response<List<CompanyDto>>

        @POST("/api/company/add_new_favourite_user_company")
        suspend fun addNewFavouriteCompany(
            @Header("Authorization") userToken: String,
            @Body favouriteCompanyAdditionRequestDto: FavouriteCompanyAdditionRequestDto
        ): Response<FavouriteCompanyAdditionResponseDto>

        @HTTP(method = "DELETE", path = "/api/company/remove_favourite_user_company", hasBody = true)
        suspend fun removeFavouriteCompany(
            @Header("Authorization") userToken: String,
            @Body favouriteCompanyRemovalRequestDto: FavouriteCompanyRemovalRequestDto
        ): Response<FavouriteCompanyRemovalResponseDto>


        @POST("/api/company/require_prediction")
        suspend fun requirePrediction(
            @Header("Authorization") userToken: String,
            @Body predictionRequestDto: PredictionRequestDto
        ): Response<PredictionResponseDto>

        @POST("/api/company/get_historical_data_close_price")
        suspend fun getHistoricalDataClosePrice(
            @Header("Authorization") userToken: String,
            @Body historicalDataClosePriceDto: HistoricalDataClosePriceDto
        ): Response<List<DateClosePrice>>




        @GET("http://newsapi.org/v2/top-headlines")
        suspend fun getRecentNews(
            @Query("apiKey") apiKey: String,
            @Query("country") country: String = "us",
            @Query("category") category: String = "business"
        ): Response<NewsResponse>
    }
}
