package ubb.cscluj.financialforecasting.database_persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import ubb.cscluj.financialforecasting.model.Company

@Dao
interface CompanyDao {
    @Query("SELECT * FROM companies")
    fun getAllCompanies(): LiveData<List<Company>>

    @Query("SELECT * FROM companies WHERE ready_for_prediction = 1")
    fun getAllCompaniesReadyForPrediction(): LiveData<List<Company>>



    @Query("SELECT COUNT(*) FROM companies")
    suspend fun getCountCompanies(): Long

    @Query("SELECT * FROM companies WHERE id = :companyId")
    suspend fun findCompanyById(companyId: Long): Company



    @Query("DELETE FROM companies")
    suspend fun clearDatabaseTable()



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanyList(companyList: List<Company>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(savedCompany: Company)

    @Update
    suspend fun updateCompany(company: Company)
}