package com.mthsgimenez.fitcontrol.person;

import com.mthsgimenez.fitcontrol.user.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class}
)
public interface PersonMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "cpf", source = "cpf")
    @Mapping(target = "birthDate", source = "birthDate")
    @Mapping(target = "user", source = "user")
    PersonResponseDTO toResponseDTO(Person person);
}