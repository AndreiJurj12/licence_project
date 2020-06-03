package ubb.cscluj.financialforecasting.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey(autoGenerate = true)  @ColumnInfo(name = "id") var id: Long = 0,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "stock_ticker_symbol") var stockTickerSymbol: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "founded_year") var foundedYear: Long ,
    @ColumnInfo(name = "url_link") var urlLink: String,
    @ColumnInfo(name = "url_logo") var urlLogo: String,
    @ColumnInfo(name = "csv_data_path")var csvDataPath: String,
    @ColumnInfo(name = "ready_for_prediction") var readyForPrediction: Boolean = false
)