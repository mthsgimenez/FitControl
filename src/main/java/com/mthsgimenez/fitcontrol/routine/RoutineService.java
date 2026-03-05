package com.mthsgimenez.fitcontrol.routine;

import com.mthsgimenez.fitcontrol.exercise.Exercise;
import com.mthsgimenez.fitcontrol.exercise.ExerciseService;
import com.mthsgimenez.fitcontrol.infra.exception.NotFoundWithIdentifierException;
import com.mthsgimenez.fitcontrol.member.Member;
import com.mthsgimenez.fitcontrol.member.MemberRepository;
import com.mthsgimenez.fitcontrol.user.RoleType;
import com.mthsgimenez.fitcontrol.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final MemberRepository memberRepository;
    private final ExerciseService exerciseService;

    public RoutineService(
            RoutineRepository routineRepository,
            MemberRepository memberRepository,
            ExerciseService exerciseService
    ) {
        this.routineRepository = routineRepository;
        this.memberRepository = memberRepository;
        this.exerciseService = exerciseService;
    }

    public Routine createRoutine(RoutineDTO dto, User authUser) {
        Member member = determineMember(dto.memberId(), authUser);

        Routine routine = new Routine();
        routine.setName(dto.name());
        routine.setMember(member);
        routine.setCreatedBy(authUser);

        buildDays(routine, dto.days());

        return routineRepository.save(routine);
    }

    private Member determineMember(Integer memberId, User authUser) {
        boolean isInstructor = authUser.hasRole(RoleType.INSTRUCTOR) ||
                authUser.hasRole(RoleType.OWNER);
        boolean isMember = authUser.hasRole(RoleType.MEMBER);

        if (isInstructor && memberId != null) {
            return memberRepository.findById(memberId)
                    .orElseThrow(() -> new NotFoundWithIdentifierException(Member.class.getSimpleName(), memberId));
        }

        if (isMember) {
            return memberRepository.findByPerson_User(authUser)
                    .orElseThrow(() -> new IllegalStateException("No member found for current user"));
        }

        throw new IllegalStateException("Cannot determine member for routine creation");
    }

    private void buildDays(Routine routine, List<RoutineDTO.RoutineDayDTO> dayDTOs) {
        if (dayDTOs == null) return;

        for (RoutineDTO.RoutineDayDTO dayDTO : dayDTOs) {
            RoutineDay day = new RoutineDay();
            day.setDayOrder(dayDTO.dayOrder());
            routine.addDay(day);

            for (RoutineDTO.RoutineExerciseDTO exDTO : dayDTO.exercises()) {
                RoutineDayExercise ex = new RoutineDayExercise();
                ex.setExerciseOrder(exDTO.exerciseOrder());
                ex.setReps(exDTO.reps());
                ex.setSeries(exDTO.series());
                ex.setNotes(exDTO.notes());

                Exercise exercise = exerciseService.getExerciseById(exDTO.exerciseId());
                ex.setExercise(exercise);

                day.addExercise(ex);
            }
        }
    }
}
