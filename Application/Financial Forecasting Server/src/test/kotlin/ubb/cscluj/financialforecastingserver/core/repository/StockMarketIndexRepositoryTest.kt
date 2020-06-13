package ubb.cscluj.financialforecastingserver.core.repository

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import ubb.cscluj.financialforecastingserver.core.model.StockMarketIndex


@DataJpaTest
class StockMarketIndexRepositoryTest{
    @Autowired
    lateinit var entityManager: TestEntityManager
    @Autowired
    lateinit var stockMarketIndexRepository: StockMarketIndexRepository

    @Test
    fun `When index exists, found index`() {
        val newIndex = StockMarketIndex(name = "test@gmail.com",
                stockTickerSymbol = "Symbol",
                csvDataPath = "C://path")
        entityManager.persist(newIndex)
        entityManager.flush()
        val foundIndex = stockMarketIndexRepository.findStockMarketIndexByStockTickerSymbol("Symbol")
        assert(newIndex == foundIndex)
    }

    @Test
    fun `When index with that symbol doesn't exist, no found index`() {
        val newIndex = StockMarketIndex(name = "test@gmail.com",
                stockTickerSymbol = "Symbol2",
                csvDataPath = "C://path")
        entityManager.persist(newIndex)
        entityManager.flush()
        val foundIndex = stockMarketIndexRepository.findStockMarketIndexByStockTickerSymbol("Symbol")
        assert(foundIndex == null)
    }
}
