package com.mthsgimenez.fitcontrol.subscription;

import com.mthsgimenez.fitcontrol.member.Member;
import com.mthsgimenez.fitcontrol.membershipplan.MembershipPlanMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MembershipPlanMapper.class})
public interface SubscriptionMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "gatewayStatus", source = "gatewayStatus")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    @Mapping(target = "membershipPlan", source = "membershipPlan")
    @Mapping(target = "members", source = "members")
    SubscriptionResponseDTO toDto(Subscription subscription);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "person.name")
    @Mapping(target = "lastName", source = "person.lastName")
    SubscriptionMemberDTO toMemberDto(Member member);
}
