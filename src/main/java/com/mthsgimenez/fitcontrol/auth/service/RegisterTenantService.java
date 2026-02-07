package com.mthsgimenez.fitcontrol.auth.service;

import com.mthsgimenez.fitcontrol.auth.dto.EmailVerificationDTO;
import com.mthsgimenez.fitcontrol.auth.dto.TenantRegisterDTO;
import com.mthsgimenez.fitcontrol.auth.dto.CreateUserDTO;
import com.mthsgimenez.fitcontrol.auth.enums.RoleType;
import com.mthsgimenez.fitcontrol.auth.exception.EmailNotVerifiedException;
import com.mthsgimenez.fitcontrol.infra.cache.CacheService;
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
    private final CacheService cacheService;

    public RegisterTenantService(
            UserService userService,
            TenantService tenantService,
            EmailVerificationService emailVerificationService,
            CacheService cacheService
    ) {
        this.userService = userService;
        this.tenantService = tenantService;
        this.emailVerificationService = emailVerificationService;
        this.cacheService = cacheService;
    }

    @Transactional
    public void registerNewTenant(TenantRegisterDTO data) throws EmailNotVerifiedException {
        EmailVerificationDTO emailVerificationData = new EmailVerificationDTO(
                data.verificationId(),
                data.email(),
                data.verificationCode()
        );

        emailVerificationService.verifyEmail(emailVerificationData);

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
        cacheService.delete("email_verification:" + data.verificationId().toString());
    }
}