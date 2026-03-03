package com.mthsgimenez.fitcontrol.member;

import com.mthsgimenez.fitcontrol.infra.exception.FKConstraintViolationException;
import com.mthsgimenez.fitcontrol.infra.exception.NotFoundWithIdentifierException;
import com.mthsgimenez.fitcontrol.person.Person;
import com.mthsgimenez.fitcontrol.user.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final RoleService roleService;
    private final UserRepository userRepository;

    public MemberService(MemberRepository memberRepository,
                         RoleService roleService,
                         UserRepository userRepository) {
        this.memberRepository = memberRepository;
        this.roleService = roleService;
        this.userRepository = userRepository;
    }

    @Transactional
    public Member createMember(MemberDTO data) {
        Person person = data.person();
        User user = person.getUser();

        Set<Role> currentRoles = user.getRoles();
        currentRoles.add(roleService.enumToEntity(RoleType.MEMBER));

        user.setRoles(currentRoles);
        userRepository.save(user);

        Member member = new Member();
        member.setPerson(person);
        member.setGoal(data.goal());
        member.setTrainingLevel(data.trainingLevel());
        member.setRestrictions(data.restrictions());

        return memberRepository.save(member);
    }

    @Transactional
    public Member editMember(Integer memberId, UpdateMemberDTO data) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new NotFoundWithIdentifierException(Member.class.getSimpleName(), memberId));

        member.setGoal(data.goal());
        member.setTrainingLevel(data.trainingLevel());
        if (data.restrictions() != null) {
            member.setRestrictions(data.restrictions());
        }

        return memberRepository.save(member);
    }

    public Member findById(Integer memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new NotFoundWithIdentifierException(Member.class.getSimpleName(), memberId));
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    @Transactional
    public void deleteById(Integer memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new NotFoundWithIdentifierException(Member.class.getSimpleName(), memberId));

        User user = member.getPerson().getUser();

        user.getRoles().removeIf(
                role -> roleService.entityToEnum(role) == RoleType.MEMBER
        );

        try {
            memberRepository.delete(member);
        } catch (DataIntegrityViolationException e) {
            throw new FKConstraintViolationException(Member.class.getSimpleName(), memberId);
        }
    }
}