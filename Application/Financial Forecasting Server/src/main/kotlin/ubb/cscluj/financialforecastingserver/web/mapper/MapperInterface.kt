package ubb.cscluj.financialforecastingserver.web.mapper

interface MapperInterface<Model, Dto> {
    fun convertDtoToModel(dto: Dto): Model
    fun convertModelToDto(model: Model): Dto
}
