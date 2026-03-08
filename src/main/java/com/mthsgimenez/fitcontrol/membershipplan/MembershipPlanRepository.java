package com.mthsgimenez.fitcontrol.membershipplan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Integer> {
    List<MembershipPlan> findAllByIsActiveTrue();
    boolean existsByName(String name);
}