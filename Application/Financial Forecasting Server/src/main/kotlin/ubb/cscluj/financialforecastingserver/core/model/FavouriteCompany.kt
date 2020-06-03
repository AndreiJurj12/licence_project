package ubb.cscluj.financialforecastingserver.core.model

import javax.persistence.*

@Entity
@Table(name = "favorite_companies",
        uniqueConstraints = [UniqueConstraint(columnNames = ["company", "user"])])
class FavouriteCompany {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "company", referencedColumnName = "id")
    lateinit var company: Company

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user", referencedColumnName = "id")
    lateinit var user: User
}
