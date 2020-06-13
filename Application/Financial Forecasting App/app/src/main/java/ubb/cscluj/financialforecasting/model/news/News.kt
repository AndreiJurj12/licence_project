package ubb.cscluj.financialforecasting.model.news

data class News(
    var source: NewsSource,
    var author: String?,
    var title: String?,
    var description: String?,
    var url: String,
    var urlToImage: String?,
    var content: String?,
    var publishedAt: String
)