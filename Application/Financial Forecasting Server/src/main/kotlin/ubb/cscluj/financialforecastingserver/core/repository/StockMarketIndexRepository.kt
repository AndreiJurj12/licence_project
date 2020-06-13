package ubb.cscluj.financialforecastingserver.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ubb.cscluj.financialforecastingserver.core.model.StockMarketIndex

@Repository
interface StockMarketIndexRepository : JpaRepository<StockMarketIndex, Long> {
    @Query("SELECT stu FROM StockMarketIndex stu WHERE stu.stockTickerSymbol = :stockTickerSymbol")
    fun findStockMarketIndexByStockTickerSymbol(@Param("stockTickerSymbol") stockTickerSymbol: String): StockMarketIndex?
}
