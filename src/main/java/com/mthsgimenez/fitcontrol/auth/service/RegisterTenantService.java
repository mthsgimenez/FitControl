package com.mthsgimenez.fitcontrol.auth.service;

import com.mthsgimenez.fitcontrol.auth.dto.EmailVerificationDTO;
import com.mthsgimenez.fitcontrol.auth.dto.TenantRegisterDTO;
import com.mthsgimenez.fitcontrol.auth.dto.UserRegisterDTO;
import com.mthsgimenez.fitcontrol.auth.exception.EmailNotVerifiedException;
import com.mthsgimenez.fitcontrol.auth.repository.RoleRepository;
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

    private final UserRegisterService userRegisterService;
    private final TenantService tenantService;
    private final EmailVerificationService emailVerificationService;
    private final RoleRepository roleRepository;
    private final CacheService cacheService;

    public RegisterTenantService(
            UserRegisterService userRegisterService,
            TenantService tenantService,
            EmailVerificationService emailVerificationService,
            RoleRepository roleRepository,
            CacheService cacheService
    ) {
        this.userRegisterService = userRegisterService;
        this.tenantService = tenantService;
        this.emailVerificationService = emailVerificationService;
        this.roleRepository = roleRepository;
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

        UserRegisterDTO userData = new UserRegisterDTO(
                data.email(),
                data.password(),
                // TODO: mover roleRepository para o UserRegisterService
                Set.of(roleRepository.findByName("ROLE_OWNER")
                        .orElseThrow(() -> new RuntimeException("ROLE_OWNER not found in database"))),
                newTenant
        );

        userRegisterService.registerNewUser(userData);

        cacheService.delete("email_verification:" + data.verificationId().toString());
    }
}
