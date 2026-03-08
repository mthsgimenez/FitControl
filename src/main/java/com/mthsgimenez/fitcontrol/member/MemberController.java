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
    private final MemberService memberService;

    public MemberController(
            MemberRegistrationService memberRegistrationService,
            UserRepository userRepository,
            MemberService memberService
    ) {
        this.memberRegistrationService = memberRegistrationService;
        this.userRepository = userRepository;
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
        UUID userUUID = UUID.fromString(jwtAuth.getToken().getSubject());
        User authUser = userRepository.findByUuid(userUUID)
                .orElseThrow(() -> new RuntimeException("Something went wrong"));
        return ResponseEntity.ok(
                memberRegistrationService.registerNewMemberAsDto(data, authUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberResponseDTO> editMember(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateMemberDTO data
    ) {
        return ResponseEntity.ok(memberService.editMemberAsDto(id, data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponseDTO> getMember(@PathVariable Integer id) {
        return ResponseEntity.ok(memberService.findByIdAsDto(id));
    }

    @GetMapping
    public ResponseEntity<List<MemberResponseDTO>> getMembers() {
        return ResponseEntity.ok(memberService.findAllAsDto());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Integer id) {
        memberService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}