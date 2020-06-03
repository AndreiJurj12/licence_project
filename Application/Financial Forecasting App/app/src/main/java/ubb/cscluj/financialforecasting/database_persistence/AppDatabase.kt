package ubb.cscluj.financialforecasting.database_persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ubb.cscluj.financialforecasting.model.Company
import ubb.cscluj.financialforecasting.model.FavouriteCompany
import ubb.cscluj.financialforecasting.model.FeedbackMessage

@Database(entities = [FeedbackMessage::class, Company::class, FavouriteCompany::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun feedbackMessageDao(): FeedbackMessageDao
    abstract fun companyDao(): CompanyDao
    abstract fun favouriteCompanyDao(): FavouriteCompanyDao

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                return Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "FinancialForecasting.db"
                )
                    //.allowMainThreadQueries()
                    .build()
                    .also { appDatabase -> instance = appDatabase }
            }
        }
    }
}