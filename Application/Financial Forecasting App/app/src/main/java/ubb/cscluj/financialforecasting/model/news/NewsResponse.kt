package ubb.cscluj.financialforecasting.model.news

data class NewsResponse(
    var status: String,
    var totalResults: Long,
    var articles: List<News>
)