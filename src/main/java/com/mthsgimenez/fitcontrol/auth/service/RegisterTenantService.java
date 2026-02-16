package com.mthsgimenez.fitcontrol.auth.service;

import com.mthsgimenez.fitcontrol.auth.dto.CreateUserDTO;
import com.mthsgimenez.fitcontrol.auth.dto.TenantRegisterDTO;
import com.mthsgimenez.fitcontrol.auth.enums.RoleType;
import com.mthsgimenez.fitcontrol.emailverification.EmailNotVerifiedException;
import com.mthsgimenez.fitcontrol.emailverification.EmailVerificationService;
import com.mthsgimenez.fitcontrol.emailverification.EmailVerificationStore;
import com.mthsgimenez.fitcontrol.tenant.dto.TenantDTO;
import com.mthsgimenez.fitcontrol.tenant.model.Tenant;
import com.mthsgimenez.fitcontrol.tenant.service.TenantService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class RegisterTenantService {

    private final UserService userService;
    private final TenantService tenantService;
    private final EmailVerificationService emailVerificationService;

    public RegisterTenantService(
            UserService userService,
            TenantService tenantService,
            EmailVerificationService emailVerificationService,
            EmailVerificationStore emailVerificationStore
    ) {
        this.userService = userService;
        this.tenantService = tenantService;
        this.emailVerificationService = emailVerificationService;
    }

    @Transactional
    public void registerNewTenant(TenantRegisterDTO data) throws EmailNotVerifiedException {
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