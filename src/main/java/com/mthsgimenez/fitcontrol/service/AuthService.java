package com.mthsgimenez.fitcontrol.service;

import com.mthsgimenez.fitcontrol.dto.EmailDTO;
import com.mthsgimenez.fitcontrol.dto.EmailVerificationDTO;
import com.mthsgimenez.fitcontrol.dto.LoginDTO;
import com.mthsgimenez.fitcontrol.dto.TenantRegisterDTO;
import com.mthsgimenez.fitcontrol.event.TenantCreatedEvent;
import com.mthsgimenez.fitcontrol.exception.EmailNotVerifiedException;
import com.mthsgimenez.fitcontrol.model.Tenant;
import com.mthsgimenez.fitcontrol.model.User;
import com.mthsgimenez.fitcontrol.repository.RoleRepository;
import com.mthsgimenez.fitcontrol.repository.TenantRepository;
import com.mthsgimenez.fitcontrol.repository.UserRepository;
import com.mthsgimenez.fitcontrol.util.JWTUtil;
import com.mthsgimenez.fitcontrol.util.OTPUtil;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final OTPUtil otpUtil;
    private final EmailService emailService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CacheService cacheService;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jWTUtil;

    public AuthService(
            UserRepository userRepository, RoleRepository roleRepository,
            TenantRepository tenantRepository,
            PasswordEncoder passwordEncoder,
            OTPUtil otpUtil,
            EmailService emailService,
            ApplicationEventPublisher applicationEventPublisher,
            CacheService cacheService,
            ObjectMapper objectMapper, MessageSource messageSource, AuthenticationManager authenticationManager,
            JWTUtil jWTUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpUtil = otpUtil;
        this.emailService = emailService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.cacheService = cacheService;
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
        this.authenticationManager = authenticationManager;
        this.jWTUtil = jWTUtil;
    }

    public UUID sendVerificationCode(EmailDTO email) throws MessagingException {
        String code = otpUtil.generateOtp();
        String hashedCode = passwordEncoder.encode(code);
        UUID verificationId = UUID.randomUUID();

        EmailVerificationDTO data = new EmailVerificationDTO(
                verificationId,
                email.email(),
                hashedCode
        );

        String cacheKey = "email_verification:" + verificationId.toString();
        cacheService.set(cacheKey, data, Duration.ofMinutes(5));

        emailService.sendOTPEmail(email.email(), code);

        log.info("Email verification has been sent to {}", email.email());

        return verificationId;
    }

    public boolean isEmailVerified(TenantRegisterDTO data) {
        String cacheKey = "email_verification:" + data.verificationId().toString();
        Object obj = cacheService.getObject(cacheKey);

        if (obj == null) {
            return false;
        }

        EmailVerificationDTO verificationData = objectMapper.convertValue(obj, EmailVerificationDTO.class);

        if (!data.email().equals(verificationData.email())) {
            return false;
        }

        return passwordEncoder.matches(data.verificationCode(), verificationData.hashedCode());
    }

    @Transactional
    public void registerTenant(TenantRegisterDTO data) throws EmailNotVerifiedException {
        if (!isEmailVerified(data)) {
            throw new EmailNotVerifiedException(
                    messageSource.getMessage("exception.email-not-verified-exception", null, LocaleContextHolder.getLocale())
            );
        }

        UUID tenantUUID = UUID.randomUUID();
        String schemaName = "tenant_" + tenantUUID.toString().split("-")[0];

        Tenant newTenant =  new Tenant();
        newTenant.setUuid(tenantUUID);
        newTenant.setCnpj(data.cnpj());
        newTenant.setLegalName(data.legalName());
        newTenant.setTradeName(data.tradeName());
        newTenant.setPostalCode(data.postalCode());
        newTenant.setSchemaName(schemaName);
        tenantRepository.save(newTenant);

        log.info("New tenant registered: {}\nSchema: {}", newTenant.getCnpj(), newTenant.getSchemaName());

        User newUser = new User();
        newUser.setEmail(data.email());
        newUser.setTenant(newTenant);
        String passwordHash = passwordEncoder.encode(data.password());
        newUser.setPasswordHash(passwordHash);
        newUser.setRoles(Set.of(roleRepository.findByName("ROLE_OWNER")));
        userRepository.save(newUser);

        String cacheKey = "email_verification:" + data.verificationId().toString();
        cacheService.delete(cacheKey);

        applicationEventPublisher.publishEvent(
                new TenantCreatedEvent(schemaName)
        );
    }

    public String login(LoginDTO data) {
        var usernamePasswordToken = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = authenticationManager.authenticate(usernamePasswordToken);

        return jWTUtil.generateToken((User) auth.getPrincipal());
    }
}
