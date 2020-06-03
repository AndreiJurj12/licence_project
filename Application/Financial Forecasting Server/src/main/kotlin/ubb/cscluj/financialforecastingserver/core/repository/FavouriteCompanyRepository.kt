package ubb.cscluj.financialforecastingserver.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ubb.cscluj.financialforecastingserver.core.model.Company
import ubb.cscluj.financialforecastingserver.core.model.FavouriteCompany
import ubb.cscluj.financialforecastingserver.core.model.User

@Repository
interface FavouriteCompanyRepository : JpaRepository<FavouriteCompany, Long> {
    @Query("SELECT fc FROM FavouriteCompany fc WHERE fc.user = :user AND fc.company = :company")
    fun getFavouriteCompanyByUserAndCompany(@Param("user") user: User, @Param("company") company: Company): FavouriteCompany?
}
