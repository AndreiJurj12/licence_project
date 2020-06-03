package ubb.cscluj.financialforecasting.database_persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import ubb.cscluj.financialforecasting.model.Company
import ubb.cscluj.financialforecasting.model.FavouriteCompany

@Dao
interface FavouriteCompanyDao {
    @Query("SELECT * FROM favourite_companies")
    fun getAllFavouriteCompanies(): LiveData<List<FavouriteCompany>>

    @Query("SELECT * FROM favourite_companies WHERE ready_for_prediction = 1")
    fun getAllFavouriteCompaniesReadyForPrediction(): LiveData<List<FavouriteCompany>>



    @Query("SELECT COUNT(*) FROM favourite_companies")
    suspend fun getCountFavouriteCompanies(): Long

    @Query("SELECT * FROM favourite_companies WHERE id = :companyId")
    suspend fun findFavouriteCompanyById(companyId: Long): FavouriteCompany



    @Query("DELETE FROM favourite_companies")
    suspend fun clearDatabaseTable()



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavouriteCompanyList(favouriteCompanyList: List<FavouriteCompany>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavouriteCompany(savedFavouriteCompany: FavouriteCompany)

    @Update
    suspend fun updateFavouriteCompany(favouriteCompany: FavouriteCompany)
}