package com.mthsgimenez.fitcontrol.exercise;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExerciseMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "categoryName", source = "category.name")
    ExerciseResponseDTO toResponseDTO(Exercise exercise);
}