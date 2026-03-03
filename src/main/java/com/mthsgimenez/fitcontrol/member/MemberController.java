package com.mthsgimenez.fitcontrol.member;

import com.mthsgimenez.fitcontrol.user.User;
import com.mthsgimenez.fitcontrol.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/member")
@PreAuthorize("hasRole('MANAGER')")
public class MemberController {

    private final MemberRegistrationService memberRegistrationService;
    private final UserRepository userRepository;
    private final MemberMapper memberMapper;
    private final MemberService memberService;

    public MemberController(
            MemberRegistrationService memberRegistrationService,
            UserRepository userRepository,
            MemberMapper memberMapper,
            MemberService memberService
    ) {
        this.memberRegistrationService = memberRegistrationService;
        this.userRepository = userRepository;
        this.memberMapper = memberMapper;
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<MemberResponseDTO> registerMember(
            @Valid @RequestBody MemberRegistrationRequestDTO data,
            Authentication auth
    ) {
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            throw new RuntimeException("Unauthorized");
        }

        String userUUIDString = jwtAuth.getToken().getSubject();
        UUID userUUID = UUID.fromString(userUUIDString);
        User authUser = userRepository.findByUuid(userUUID)
                .orElseThrow(() -> new RuntimeException("Something went wrong"));

        Member newMember = memberRegistrationService.registerNewMember(data, authUser);
        MemberResponseDTO responseDTO = memberMapper.toResponseDTO(newMember);

        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberResponseDTO> editMember(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateMemberDTO data
    ) {
        Member updatedMember = memberService.editMember(id, data);
        MemberResponseDTO response = memberMapper.toResponseDTO(updatedMember);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponseDTO> getMember(@PathVariable Integer id) {
        Member member = memberService.findById(id);
        MemberResponseDTO response = memberMapper.toResponseDTO(member);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MemberResponseDTO>> getMembers() {
        List<Member> members = memberService.findAll();
        List<MemberResponseDTO> responseList = members.stream()
                .map(memberMapper::toResponseDTO)
                .toList();

        return ResponseEntity.ok(responseList);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Integer id) {
        memberService.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
