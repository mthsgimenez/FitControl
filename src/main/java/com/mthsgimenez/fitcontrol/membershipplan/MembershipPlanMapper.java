package com.mthsgimenez.fitcontrol.membershipplan;

import com.mthsgimenez.fitcontrol.subscription.MembershipPlanSummaryDTO;
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

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "durationValue", source = "durationValue")
    @Mapping(target = "maxBeneficiaries", source = "maxBeneficiaries")
    MembershipPlanSummaryDTO toSummaryDto(MembershipPlan plan);
}
