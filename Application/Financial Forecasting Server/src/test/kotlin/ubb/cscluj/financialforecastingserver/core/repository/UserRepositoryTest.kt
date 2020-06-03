package ubb.cscluj.financialforecastingserver.core.repository

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isNullOrBlank
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import ubb.cscluj.financialforecastingserver.core.model.User

@DataJpaTest
class UserRepositoryTest @Autowired constructor(
        val entityManager: TestEntityManager,
        val userRepository: UserRepository
) {

    @Test
    fun `When account exists, found account`() {
        val newUser = User(email = "test@gmail.com",
                password = "12345678",
                isAdmin = true)
        entityManager.persist(newUser)
        entityManager.flush()
        val foundUser = userRepository.getUserByEmail("test@gmail.com")
        assert(newUser == foundUser)
        entityManager.remove(newUser)
    }

    @Test
    fun `When account doesn't exists, no found account`() {
        /*
        val newUser = User(email = "test@gmail.com",
                password = "12345678",
                isAdmin = true)
        entityManager.persist(newUser)
        */
        entityManager.flush()
        val foundUser = userRepository.getUserByEmail("test@gmail.com")
        assert(foundUser == null)
    }
}
