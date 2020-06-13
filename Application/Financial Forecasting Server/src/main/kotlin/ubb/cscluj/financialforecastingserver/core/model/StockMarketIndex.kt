package ubb.cscluj.financialforecastingserver.core.model

import javax.persistence.*

@Entity
@Table(name = "stockmarket_indexes")
class StockMarketIndex(
        @Column(name = "name", nullable = false) var name: String,
        @Column(name = "stock_ticker_symbol", nullable = false) var stockTickerSymbol: String,
        @Column(name = "csv_data_path", nullable = false) var csvDataPath: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
}
