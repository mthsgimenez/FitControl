package com.mthsgimenez.fitcontrol.workout;

import com.mthsgimenez.fitcontrol.exercise.Exercise;
import com.mthsgimenez.fitcontrol.exercise.ExerciseRepository;
import com.mthsgimenez.fitcontrol.exercise.ExerciseService;
import com.mthsgimenez.fitcontrol.infra.exception.NotFoundWithIdentifierException;
import com.mthsgimenez.fitcontrol.member.Member;
import com.mthsgimenez.fitcontrol.member.MemberRepository;
import com.mthsgimenez.fitcontrol.user.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Transactional
public class WorkoutService {

    private final MemberRepository memberRepository;
    private final WorkoutRepository workoutRepository;
    private final ExerciseService exerciseService;

    public WorkoutService(
            MemberRepository memberRepository,
            WorkoutRepository workoutRepository,
            ExerciseService exerciseService
    ) {
        this.memberRepository = memberRepository;
        this.workoutRepository = workoutRepository;
        this.exerciseService = exerciseService;
    }

    public Workout createWorkout(User authUser) {
        Member member = memberRepository.findByPerson_User(authUser)
                .orElseThrow(() -> new IllegalStateException("Member entity not found for user"));

        Workout newWorkout = new Workout();
        newWorkout.setWorkoutDate(LocalDate.now());
        newWorkout.setMember(member);

        return workoutRepository.save(newWorkout);
    }

    public Workout addExerciseToWorkout(Integer workoutId, Integer exerciseId, User authUser)  {
        Member member = memberRepository.findByPerson_User(authUser)
                .orElseThrow(() -> new IllegalStateException("Member entity not found for user"));

        Workout workout = workoutRepository.findByIdAndMemberId(workoutId, member.getId())
                .orElseThrow(() -> new NotFoundWithIdentifierException(Workout.class.getSimpleName(), workoutId));

        Exercise exercise = exerciseService.getExerciseById(exerciseId);

        PerformedExercise performedExercise = new PerformedExercise();
        performedExercise.setExercise(exercise);
        workout.addExercise(performedExercise);

        return workout;
    }

    public Workout addSetToExercise(Integer workoutId, Integer exerciseId, SetDTO data, User authUser) {
        Member member = memberRepository.findByPerson_User(authUser)
                .orElseThrow(() -> new IllegalStateException("Member entity not found for user"));

        Workout workout = workoutRepository.findByIdAndMemberId(workoutId, member.getId())
                .orElseThrow(() -> new NotFoundWithIdentifierException(Workout.class.getSimpleName(), workoutId));

        PerformedExercise exercise = workout.getExercises().stream()
                .filter(e -> e.getExercise().getId().equals(exerciseId))
                .findFirst()
                .orElseThrow(() -> new NotFoundWithIdentifierException(Exercise.class.getSimpleName(), exerciseId));

        PerformedSet set = new PerformedSet();
        set.setRepetitions(data.repetitions());
        set.setWeight(data.weight());
        set.setNotes(data.notes());
        exercise.addSet(set);

        return workout;
    }
}