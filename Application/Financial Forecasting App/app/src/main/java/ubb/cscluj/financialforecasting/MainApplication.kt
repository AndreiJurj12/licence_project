package ubb.cscluj.financialforecasting

import android.app.Application
import android.content.Context
import ubb.cscluj.financialforecasting.database_persistence.AppDatabase
import ubb.cscluj.financialforecasting.service.NetworkService

class MainApplication : Application() {
    lateinit var databaseReference: AppDatabase
    //lateinit var connectivityService: ConnectivityService
    lateinit var networkService: NetworkService
    lateinit var context: Context
    lateinit var userToken: String
    lateinit var email: String
    var isAdmin: Boolean = false
    var userId = -1L

    override fun onCreate() {
        super.onCreate()

        databaseReference = AppDatabase.getInstance(applicationContext)
        //connectivityService = ConnectivityService.getInstance(applicationContext)
        networkService = NetworkService.getInstance(applicationContext)
        context = applicationContext
    }
}