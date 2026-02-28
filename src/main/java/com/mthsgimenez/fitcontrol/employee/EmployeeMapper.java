package com.mthsgimenez.fitcontrol.employee;

import com.mthsgimenez.fitcontrol.person.PersonMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = PersonMapper.class
)
public interface EmployeeMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "admissionDate", source = "admissionDate")
    @Mapping(target = "person", source = "person")
    EmployeeResponseDTO toResponseDTO(Employee employee);
}
