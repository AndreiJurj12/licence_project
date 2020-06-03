package ubb.cscluj.financialforecastingserver.core.model

import javax.persistence.*


@NamedEntityGraph(name = "userWithFavouriteCompaniesAndCompanyFieldsLoaded",
        attributeNodes = [
            NamedAttributeNode("email"),
            NamedAttributeNode("password"),
            NamedAttributeNode("isAdmin"),
            NamedAttributeNode("favouriteCompaniesSet", subgraph = "favouriteCompaniesAndCompanyFieldsLoadedSubGraph")],
        subgraphs = [
            NamedSubgraph(name = "favouriteCompaniesAndCompanyFieldsLoadedSubGraph",
                    attributeNodes = [NamedAttributeNode("company")]
            )
        ]
)

@Entity
@Table(name = "users")
class User(
        @Column(name = "email", nullable = false) var email: String,
        @Column(name = "password", nullable = false) var password: String,
        @Column(name = "is_admin", nullable = false) var isAdmin: Boolean
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, mappedBy = "user")
    var feedbackMessageSet: MutableSet<FeedbackMessage> = mutableSetOf()

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, mappedBy = "user")
    var favouriteCompaniesSet: MutableSet<FavouriteCompany> = mutableSetOf()


    fun addNewFavouriteCompany(company: Company) {
        val newFavoriteCompany = FavouriteCompany()
        newFavoriteCompany.company = company
        newFavoriteCompany.user = this
        favouriteCompaniesSet.add(newFavoriteCompany)
    }

    fun removeFavouriteCompany(company: Company) {
        favouriteCompaniesSet = favouriteCompaniesSet.filter { it.company != company }.toMutableSet()
        company.favouriteUserCompanySet = company.favouriteUserCompanySet.filter { it.user != this }.toMutableSet()
    }
}
