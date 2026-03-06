package com.mthsgimenez.fitcontrol.workout;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkoutMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "workoutDate", source = "workoutDate")
    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "exercises", source = "exercises")
    WorkoutFullResponseDTO toFullDto(Workout workout);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "exerciseId", source = "exercise.id")
    @Mapping(target = "exerciseName", source = "exercise.name")
    @Mapping(target = "sets", source = "sets")
    WorkoutFullResponseDTO.WorkoutExerciseDTO toExerciseDto(
            PerformedExercise exercise
    );

    @Mapping(target = "id", source = "id")
    @Mapping(target = "weight", source = "weight")
    @Mapping(target = "repetitions", source = "repetitions")
    @Mapping(target = "notes", source = "notes")
    WorkoutFullResponseDTO.WorkoutSetDTO toSetDto(
            PerformedSet set
    );

    @Mapping(target = "id", source = "id")
    @Mapping(target = "workoutDate", source = "workoutDate")
    @Mapping(target = "memberId", source = "member.id")
    WorkoutResponseDTO toSimpleDto(Workout workout);
}