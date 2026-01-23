package com.mthsgimenez.fitcontrol.service;

import com.mthsgimenez.fitcontrol.dto.TenantRegisterDTO;
import com.mthsgimenez.fitcontrol.model.Tenant;
import com.mthsgimenez.fitcontrol.model.User;
import com.mthsgimenez.fitcontrol.repository.TenantRepository;
import com.mthsgimenez.fitcontrol.repository.UserRepository;
import com.mthsgimenez.fitcontrol.util.JWTUtil;
import com.mthsgimenez.fitcontrol.util.OTPUtil;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final OTPUtil otpUtil;
    private final JWTUtil jwtUtil;
    private final EmailService emailService;
    private final SchemaService schemaService;

    public AuthService(
            UserRepository userRepository,
            TenantRepository tenantRepository,
            PasswordEncoder passwordEncoder,
            OTPUtil otpUtil,
            JWTUtil jwtUtil,
            EmailService emailService,
            SchemaService schemaService
    ) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpUtil = otpUtil;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
        this.schemaService = schemaService;
    }

    public void sendVerificationCode(String email) throws MessagingException {
        String otp = otpUtil.generateAndStoreOTP(email);
        emailService.SendOTPEmail(email, otp);
    }

    public String verifyEmail(String email, String code) {
        if (!otpUtil.verifyOtp(email, code)) {
            return "";
        }

        return jwtUtil.generateRegistrationToken(email);
    }

    @Transactional
    public void registerTenant(TenantRegisterDTO data) {
        String email = jwtUtil.verifyRegistrationTokenAndGetEmail(data.registrationToken());

        if (!data.email().equals(email) || email.isEmpty()) {
            throw new RuntimeException("Invalid email");
        }

        String schemaName = schemaService.createSchema();

        Tenant newTenant =  new Tenant();
        newTenant.setCnpj(data.cnpj());
        newTenant.setLegalName(data.legalName());
        newTenant.setTradeName(data.tradeName());
        newTenant.setPostalCode(data.postalCode());
        newTenant.setSchemaName(schemaName);

        tenantRepository.save(newTenant);

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setTenant(newTenant);
        String passwordHash = passwordEncoder.encode(data.password());
        newUser.setPasswordHash(passwordHash);
        userRepository.save(newUser);
    }
}
