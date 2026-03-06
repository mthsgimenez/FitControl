package com.mthsgimenez.fitcontrol.routine;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoutineMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "createdByUserId", source = "createdBy.id")
    @Mapping(target = "days", source = "days")
    RoutineFullResponseDTO toFullDto(Routine routine);

    @Mapping(target = "exercises", source = "exercises")
    RoutineFullResponseDTO.RoutineDayDTO toDayDto(
            RoutineDay day
    );

    @Mapping(target = "exerciseId", source = "exercise.id")
    @Mapping(target = "exerciseName", source = "exercise.name")
    @Mapping(target = "reps", source = "reps")
    @Mapping(target = "series", source = "series")
    @Mapping(target = "notes", source = "notes")
    RoutineFullResponseDTO.RoutineExerciseDTO toExerciseDto(
            RoutineDayExercise exercise
    );

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "createdByUserId", source = "createdBy.id")
    RoutineResponseDTO toSimpleDto(Routine routine);
}
