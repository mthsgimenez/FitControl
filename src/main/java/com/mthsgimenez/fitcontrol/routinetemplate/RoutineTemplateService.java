package com.mthsgimenez.fitcontrol.routinetemplate;

import com.mthsgimenez.fitcontrol.exercise.Exercise;
import com.mthsgimenez.fitcontrol.exercise.ExerciseService;
import com.mthsgimenez.fitcontrol.infra.exception.NotFoundWithIdentifierException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RoutineTemplateService {

    private final RoutineTemplateRepository routineTemplateRepository;
    private final ExerciseService exerciseService;
    private final RoutineTemplateMapper routineTemplateMapper;

    public RoutineTemplateService(
            RoutineTemplateRepository routineTemplateRepository,
            ExerciseService exerciseService,
            RoutineTemplateMapper routineTemplateMapper
    ) {
        this.routineTemplateRepository = routineTemplateRepository;
        this.exerciseService = exerciseService;
        this.routineTemplateMapper = routineTemplateMapper;
    }

    public RoutineTemplate createRoutineTemplate(RoutineTemplateDTO dto) {
        RoutineTemplate template = new RoutineTemplate();
        template.setName(dto.name());
        buildDays(template, dto.days());

        return routineTemplateRepository.save(template);
    }

    @Transactional(readOnly = true)
    public List<RoutineTemplate> getAllRoutineTemplates() {
        return routineTemplateRepository.findAll();
    }

    @Transactional(readOnly = true)
    public RoutineTemplate getRoutineTemplateById(Integer id) {
        return routineTemplateRepository.findById(id)
                .orElseThrow(() -> new NotFoundWithIdentifierException("Template", id));
    }

    public RoutineTemplate updateRoutineTemplate(
            Integer id,
            RoutineTemplateDTO dto
    ) {
        RoutineTemplate template = routineTemplateRepository.findById(id)
                .orElseThrow(() -> new NotFoundWithIdentifierException("Template", id));

        template.setName(dto.name());
        template.clearDays();
        routineTemplateRepository.flush();
        buildDays(template, dto.days());

        return template;
    }

    public void deleteRoutineTemplate(Integer id) {
        RoutineTemplate template = routineTemplateRepository.findById(id)
                .orElseThrow(() -> new NotFoundWithIdentifierException("Template", id));

        routineTemplateRepository.delete(template);
    }

    private void buildDays(RoutineTemplate template, List<RoutineTemplateDTO.TemplateDayDTO> dayDTOs) {
        if (dayDTOs == null) return;

        for (RoutineTemplateDTO.TemplateDayDTO dayDTO : dayDTOs) {
            RoutineTemplateDay day = new RoutineTemplateDay();
            template.addDay(day);

            if (dayDTO.exercises() == null) continue;

            for (RoutineTemplateDTO.TemplateExerciseDTO exerciseDTO : dayDTO.exercises()) {
                RoutineTemplateDayExercise dayExercise = new RoutineTemplateDayExercise();

                Exercise exercise = exerciseService.getExerciseById(exerciseDTO.exerciseId());
                dayExercise.setExercise(exercise);

                day.addExercise(dayExercise);
            }
        }
    }

    @Transactional(readOnly = true)
    public RoutineTemplateFullResponseDTO getRoutineTemplateByIdAsDto(Integer id) {
        return routineTemplateMapper.toFullDto(getRoutineTemplateById(id));
    }

    public RoutineTemplateFullResponseDTO createRoutineTemplateAsDto(RoutineTemplateDTO dto) {
        return routineTemplateMapper.toFullDto(createRoutineTemplate(dto));
    }

    public RoutineTemplateFullResponseDTO updateRoutineTemplateAsDto(Integer id, RoutineTemplateDTO dto) {
        return routineTemplateMapper.toFullDto(updateRoutineTemplate(id, dto));
    }

    @Transactional(readOnly = true)
    public List<RoutineTemplateResponseDTO> getAllRoutineTemplatesAsDto() {
        return routineTemplateRepository.findAll().stream()
                .map(routineTemplateMapper::toSimpleDto)
                .toList();
    }
}
