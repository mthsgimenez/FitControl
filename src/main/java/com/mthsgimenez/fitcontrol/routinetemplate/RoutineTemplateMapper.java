package com.mthsgimenez.fitcontrol.routinetemplate;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoutineTemplateMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "days", source = "days")
    RoutineTemplateFullResponseDTO toFullDto(RoutineTemplate routineTemplate);

    @Mapping(target = "dayOrder", source = "dayOrder")
    @Mapping(target = "exercises", source = "exercises")
    RoutineTemplateFullResponseDTO.TemplateDayDTO toDayDto(
            RoutineTemplateDay day
    );

    @Mapping(target = "exerciseId", source = "exercise.id")
    @Mapping(target = "exerciseName", source = "exercise.name")
    @Mapping(target = "exerciseOrder", source = "exerciseOrder")
    RoutineTemplateFullResponseDTO.TemplateExerciseDTO toExerciseDto(
            RoutineTemplateDayExercise exercise
    );

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    RoutineTemplateResponseDTO toSimpleDto(RoutineTemplate routineTemplate);
}
