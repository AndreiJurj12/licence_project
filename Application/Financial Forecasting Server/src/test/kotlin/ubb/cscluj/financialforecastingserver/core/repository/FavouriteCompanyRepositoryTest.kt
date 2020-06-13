package ubb.cscluj.financialforecastingserver.core.repository

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import ubb.cscluj.financialforecastingserver.core.model.Company
import ubb.cscluj.financialforecastingserver.core.model.FavouriteCompany
import ubb.cscluj.financialforecastingserver.core.model.User

@DataJpaTest
class FavouriteCompanyRepositoryTest {
    @Autowired
    lateinit var entityManager: TestEntityManager
    @Autowired
    lateinit var favouriteCompanyRepository: FavouriteCompanyRepository

    @Test
    fun `When favourite company by user and company exists, load it correctly`() {
        val user = User(email = "test@gmail.com",
                password = "12345678",
                isAdmin = true)
        val company = Company(name = "Name",
                stockTickerSymbol = "Symbol",
                description = "Description",
                foundedYear = 2000,
                urlLink = "www.link.com",
                urlLogo = "www.logo.com",
                csvDataPath = "C://path")
        val persistedUser = entityManager.persist(user)
        val persistedCompany = entityManager.persist(company)

        persistedUser.addNewFavouriteCompany(persistedCompany)
        entityManager.persistAndFlush(persistedUser)

        val favouriteCompany = favouriteCompanyRepository.getFavouriteCompanyByUserAndCompany(persistedUser, persistedCompany)
        assert(favouriteCompany != null)

        persistedUser.removeFavouriteCompany(company)
        entityManager.persistAndFlush(persistedUser)

        assert(favouriteCompanyRepository.getFavouriteCompanyByUserAndCompany(persistedUser, persistedCompany) != null)

        favouriteCompanyRepository.delete(favouriteCompany as FavouriteCompany)
        assert(favouriteCompanyRepository.getFavouriteCompanyByUserAndCompany(persistedUser, persistedCompany) == null)
    }


    @Test
    fun `When favourite company by user and company doesn't exist, find null`() {
        val user = User(email = "test@gmail.com",
                password = "12345678",
                isAdmin = true)
        val company = Company(name = "Name",
                stockTickerSymbol = "Symbol",
                description = "Description",
                foundedYear = 2000,
                urlLink = "www.link.com",
                urlLogo = "www.logo.com",
                csvDataPath = "C://path")
        val persistedUser = entityManager.persist(user)
        val persistedCompany = entityManager.persist(company)

        val favouriteCompany = favouriteCompanyRepository.getFavouriteCompanyByUserAndCompany(persistedUser, persistedCompany)
        assert(favouriteCompany == null)
    }
}
