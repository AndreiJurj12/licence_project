package ubb.cscluj.financialforecastingserver.core.repository

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import ubb.cscluj.financialforecastingserver.core.model.Company
import ubb.cscluj.financialforecastingserver.core.model.User

@DataJpaTest
class UserRepositoryTest{
    @Autowired
    lateinit var entityManager: TestEntityManager

    @Autowired
    lateinit var userRepository: UserRepository

    @Test
    fun `When account exists, found account`() {
        val newUser = User(email = "test@gmail.com",
                password = "12345678",
                isAdmin = true)
        entityManager.persist(newUser)
        entityManager.flush()
        val foundUser = userRepository.getUserByEmail("test@gmail.com")
        assert(newUser == foundUser)
    }

    @Test
    fun `When account doesn't exist, no found account`() {
        val newUser = User(email = "test2@gmail.com",
                password = "12345678",
                isAdmin = true)
        entityManager.persist(newUser)
        entityManager.flush()
        val foundUser = userRepository.getUserByEmail("test@gmail.com")
        assert(foundUser == null)
    }

    @Test
    fun `When retrieving account with entity graph, all fields are loaded`() {
        val newUser = User(email = "test@gmail.com",
                password = "12345678",
                isAdmin = false)
        val persistedUser = entityManager.persist(newUser)
        val company = Company(name = "Name",
                stockTickerSymbol = "Symbol",
                description = "Description",
                foundedYear = 2000,
                urlLink = "www.link.com",
                urlLogo = "www.logo.com",
                csvDataPath = "C://path")
        val persistedCompany = entityManager.persist(company)

        assert(persistedUser != null)
        assert(persistedCompany != null)

        persistedUser.addNewFavouriteCompany(persistedCompany)
        entityManager.persistAndFlush(persistedUser)

        val userWithLoadedFields = userRepository.getUserByIdWithFavouritesCompaniesSetLoaded(persistedUser.id)
        assert(userWithLoadedFields == newUser)
        assert(userWithLoadedFields!!.favouriteCompaniesSet.size == 1)
        assert(userWithLoadedFields.favouriteCompaniesSet.first().company == persistedCompany)
    }

}
