package ubb.cscluj.financialforecastingserver.core.model

import javax.persistence.*

@Entity
@Table(name = "companies")
class Company(
        @Column(name = "name", nullable = false) var name: String,
        @Column(name = "stock_ticker_symbol", nullable = false) var stockTickerSymbol: String,
        @Column(name = "description", nullable = false, length = 10000) var description: String,
        @Column(name = "founded_year", nullable = false) var foundedYear: Long,
        @Column(name = "url_link", nullable = false) var urlLink: String,
        @Column(name = "url_logo", nullable = false) var urlLogo: String,
        @Column(name = "csv_data_path", nullable = false) var csvDataPath: String,
        @Column(name = "ready_for_prediction", nullable = false) var readyForPrediction: Boolean = false
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, mappedBy = "company")
    var favouriteUserCompanySet: MutableSet<FavouriteCompany> = mutableSetOf()
}
