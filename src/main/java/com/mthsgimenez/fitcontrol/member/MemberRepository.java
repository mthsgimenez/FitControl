package com.mthsgimenez.fitcontrol.member;

import com.mthsgimenez.fitcontrol.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    Optional<Member> findByPerson_User(User user);
    Optional<Member> findByPersonUserId(Integer userId);
    Optional<Member> findByGatewayCustomerId(String gatewayCustomerId);
}
