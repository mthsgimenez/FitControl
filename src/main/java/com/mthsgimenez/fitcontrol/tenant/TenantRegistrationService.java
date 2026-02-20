package com.mthsgimenez.fitcontrol.tenant;

import com.mthsgimenez.fitcontrol.emailverification.EmailNotVerifiedException;
import com.mthsgimenez.fitcontrol.emailverification.EmailVerificationService;
import com.mthsgimenez.fitcontrol.user.CreateUserDTO;
import com.mthsgimenez.fitcontrol.user.RoleType;
import com.mthsgimenez.fitcontrol.user.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class TenantRegistrationService {

    private final UserService userService;
    private final TenantService tenantService;
    private final EmailVerificationService emailVerificationService;

    public TenantRegistrationService(
            UserService userService,
            TenantService tenantService,
            EmailVerificationService emailVerificationService
    ) {
        this.userService = userService;
        this.tenantService = tenantService;
        this.emailVerificationService = emailVerificationService;
    }

    @Transactional
    public void registerNewTenant(TenantRegisterRequestDTO data) throws EmailNotVerifiedException {
        emailVerificationService.verifyEmail(data.email(), data.verificationCode());

        TenantDTO tenantData = new TenantDTO(
                data.cnpj(),
                data.postalCode(),
                data.tradeName(),
                data.legalName()
        );

        Tenant newTenant = tenantService.createTenant(tenantData);
        log.info("New tenant registered: {}", newTenant.getUuid());

        CreateUserDTO userData = new CreateUserDTO(
                data.email(),
                data.password(),
                Set.of(RoleType.OWNER),
                newTenant
        );

        userService.createUser(userData);
        emailVerificationService.deleteVerificationForEmail(data.email());
    }
}