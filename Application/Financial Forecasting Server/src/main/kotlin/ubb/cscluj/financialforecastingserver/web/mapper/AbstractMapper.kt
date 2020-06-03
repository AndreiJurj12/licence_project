package ubb.cscluj.financialforecastingserver.web.mapper

import java.util.stream.Collectors


abstract class AbstractMapper<Model, Dto> : MapperInterface<Model, Dto> {
    fun convertModelsToDtos(models: Collection<Model>): Collection<Dto> {
        return models.stream()
                .map { model: Model -> convertModelToDto(model) }
                .collect(Collectors.toSet())
    }

    fun convertDtosToModel(dataTransferObjects: Collection<Dto>): Collection<Model> {
        return dataTransferObjects.stream()
                .map { dto: Dto -> convertDtoToModel(dto) }
                .collect(Collectors.toSet())
    }
}
