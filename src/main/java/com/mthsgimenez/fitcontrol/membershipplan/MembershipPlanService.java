package com.mthsgimenez.fitcontrol.membershipplan;

import com.mthsgimenez.fitcontrol.infra.exception.NotFoundWithIdentifierException;
import com.mthsgimenez.fitcontrol.infra.exception.UniqueConstraintViolatedException;
import com.mthsgimenez.fitcontrol.paymentgateway.StripePriceService;
import com.mthsgimenez.fitcontrol.tenant.Tenant;
import com.mthsgimenez.fitcontrol.tenant.TenantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class MembershipPlanService {

    private final MembershipPlanRepository membershipPlanRepository;
    private final StripePriceService stripePriceService;
    private final TenantRepository tenantRepository;

    public MembershipPlanService(
            MembershipPlanRepository membershipPlanRepository,
            StripePriceService stripePriceService,
            TenantRepository tenantRepository
    ) {
        this.membershipPlanRepository = membershipPlanRepository;
        this.stripePriceService = stripePriceService;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public MembershipPlan create(MembershipPlanRequestDTO data, UUID tenantUuid) {
        if (membershipPlanRepository.existsByName(data.name())) {
            throw new UniqueConstraintViolatedException(
                    "Plan", "name", data.name()
            );
        }

        Tenant tenant = tenantRepository.findByUuid(tenantUuid)
                .orElseThrow(() -> new NotFoundWithIdentifierException(
                        Tenant.class.getSimpleName(), tenantUuid
                ));

        if (tenant.getGatewayProductId() == null) {
            throw new RuntimeException(
                    "Tenant has not completed Stripe onboarding");
        }

        MembershipPlan plan = new MembershipPlan();
        plan.setName(data.name());
        plan.setPrice(data.price());
        plan.setDurationValue(data.durationValue());
        plan.setMaxBeneficiaries(
                data.maxBeneficiaries() != null ? data.maxBeneficiaries() : 1);
        plan.setIsActive(true);

        membershipPlanRepository.save(plan);

        String priceId = stripePriceService.createPrice(
                plan,
                tenant.getGatewayProductId(),
                tenant.getGatewayAccountId()
        );
        plan.setGatewayPriceId(priceId);
        return membershipPlanRepository.save(plan);
    }

    @Transactional
    public MembershipPlan update(Integer id, MembershipPlanRequestDTO data, UUID tenantUuid) {
        MembershipPlan plan = membershipPlanRepository.findById(id)
                .orElseThrow(() -> new NotFoundWithIdentifierException(
                        "Plan", id
                ));

        Tenant tenant = tenantRepository.findByUuid(tenantUuid)
                .orElseThrow(() -> new NotFoundWithIdentifierException(
                        Tenant.class.getSimpleName(), tenantUuid
                ));

        boolean priceChanged = !plan.getPrice().equals(data.price())
                || !plan.getDurationValue().equals(data.durationValue());

        plan.setName(data.name());
        plan.setPrice(data.price());
        plan.setDurationValue(data.durationValue());
        plan.setMaxBeneficiaries(
                data.maxBeneficiaries() != null ? data.maxBeneficiaries() : 1);

        if (priceChanged) {
            if (plan.getGatewayPriceId() != null) {
                stripePriceService.archivePrice(
                        plan.getGatewayPriceId(),
                        tenant.getGatewayAccountId()
                );
            }
            String newPriceId = stripePriceService.createPrice(
                    plan,
                    tenant.getGatewayProductId(),
                    tenant.getGatewayAccountId()
            );
            plan.setGatewayPriceId(newPriceId);
        }

        return membershipPlanRepository.save(plan);
    }

    @Transactional
    public void deactivate(Integer id, UUID tenantUuid) {
        MembershipPlan plan = membershipPlanRepository.findById(id)
                .orElseThrow(() -> new NotFoundWithIdentifierException(
                        "Plan", id
                ));

        Tenant tenant = tenantRepository.findByUuid(tenantUuid)
                .orElseThrow(() -> new NotFoundWithIdentifierException(
                        Tenant.class.getSimpleName(), tenantUuid
                ));

        if (plan.getGatewayPriceId() != null) {
            stripePriceService.archivePrice(
                    plan.getGatewayPriceId(),
                    tenant.getGatewayAccountId()
            );
        }

        plan.setIsActive(false);
        membershipPlanRepository.save(plan);
    }

    @Transactional(readOnly = true)
    public List<MembershipPlan> findAllActive() {
        return membershipPlanRepository.findAllByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public MembershipPlan findById(Integer id) {
        return membershipPlanRepository.findById(id)
                .orElseThrow(() -> new NotFoundWithIdentifierException(
                        "Plan", id
                ));
    }
}