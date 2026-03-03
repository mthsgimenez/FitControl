package com.mthsgimenez.fitcontrol.member;

import com.mthsgimenez.fitcontrol.person.PersonMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = PersonMapper.class
)
public interface MemberMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "person", source = "person")
    @Mapping(target = "goal", source = "goal")
    @Mapping(target = "restrictions", source = "restrictions")
    @Mapping(target = "trainingLevel", source = "trainingLevel")
    MemberResponseDTO toResponseDTO(Member member);
}
