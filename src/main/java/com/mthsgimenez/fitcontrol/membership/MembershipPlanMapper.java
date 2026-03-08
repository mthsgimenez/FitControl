package com.mthsgimenez.fitcontrol.membership;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MembershipPlanMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "durationValue", source = "durationValue")
    @Mapping(target = "maxBeneficiaries", source = "maxBeneficiaries")
    @Mapping(target = "isActive", source = "isActive")
    MembershipPlanResponseDTO toDto(MembershipPlan plan);
}
