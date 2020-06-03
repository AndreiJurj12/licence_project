package ubb.cscluj.financialforecastingserver.core.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ubb.cscluj.financialforecastingserver.core.model.User

@Repository
interface UserRepository : JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.email = :email")
    fun getUserByEmail(@Param("email") email: String): User?

    @EntityGraph(value = "userWithFavouriteCompaniesAndCompanyFieldsLoaded", type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT u FROM User u WHERE u.id = :userId")
    fun getUserByIdWithFavouritesCompaniesSetLoaded(@Param("userId") userId: Long): User?
}
