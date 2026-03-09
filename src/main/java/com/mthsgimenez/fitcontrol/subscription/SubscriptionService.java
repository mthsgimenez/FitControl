package com.mthsgimenez.fitcontrol.subscription;

import com.mthsgimenez.fitcontrol.infra.exception.NotFoundWithIdentifierException;
import com.mthsgimenez.fitcontrol.member.Member;
import com.mthsgimenez.fitcontrol.member.MemberRepository;
import com.mthsgimenez.fitcontrol.membershipplan.MembershipPlan;
import com.mthsgimenez.fitcontrol.membershipplan.MembershipPlanMapper;
import com.mthsgimenez.fitcontrol.membershipplan.MembershipPlanRepository;
import com.mthsgimenez.fitcontrol.payment.PaymentMapper;
import com.mthsgimenez.fitcontrol.payment.PaymentRepository;
import com.mthsgimenez.fitcontrol.payment.PaymentResponseDTO;
import com.mthsgimenez.fitcontrol.paymentgateway.StripeCheckoutService;
import com.mthsgimenez.fitcontrol.paymentgateway.StripeCustomerService;
import com.mthsgimenez.fitcontrol.tenant.Tenant;
import com.mthsgimenez.fitcontrol.tenant.TenantRepository;
import com.mthsgimenez.fitcontrol.user.User;
import com.stripe.model.SubscriptionItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final StripeCustomerService stripeCustomerService;
    private final StripeCheckoutService stripeCheckoutService;
    private final TenantRepository tenantRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final MembershipPlanMapper membershipPlanMapper;

    public SubscriptionService(
            SubscriptionRepository subscriptionRepository,
            MemberRepository memberRepository,
            MembershipPlanRepository membershipPlanRepository,
            StripeCustomerService stripeCustomerService,
            StripeCheckoutService stripeCheckoutService,
            TenantRepository tenantRepository,
            SubscriptionMapper subscriptionMapper,
            PaymentRepository paymentRepository,
            PaymentMapper paymentMapper,
            MembershipPlanMapper membershipPlanMapper
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.memberRepository = memberRepository;
        this.membershipPlanRepository = membershipPlanRepository;
        this.stripeCustomerService = stripeCustomerService;
        this.stripeCheckoutService = stripeCheckoutService;
        this.tenantRepository = tenantRepository;
        this.subscriptionMapper = subscriptionMapper;
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.membershipPlanMapper = membershipPlanMapper;
    }

    public String initiateCheckout(Integer planId, User user, UUID tenantUuid) {
        Tenant tenant = tenantRepository.findByUuid(tenantUuid)
                .orElseThrow(() -> new NotFoundWithIdentifierException(
                        Tenant.class.getSimpleName(), tenantUuid
                ));

        MembershipPlan plan = membershipPlanRepository.findById(planId)
                .orElseThrow(() -> new NotFoundWithIdentifierException(
                        "Plan", planId
                ));

        if (!plan.getIsActive()) {
            throw new IllegalStateException("Plan is no longer active");
        }

        Member member = memberRepository.findByPersonUserId(user.getId())
                .orElseThrow(() -> new NotFoundWithIdentifierException(
                        Member.class.getSimpleName(), user.getId()
                ));

        if (subscriptionRepository.existsByPayerIdAndStatusIn(
                member.getId(),
                List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PENDING))) {
            throw new IllegalStateException("Member already has an active subscription");
        }

        if (member.getGatewayCustomerId() == null) {
            String customerId = stripeCustomerService.createCustomer(
                    member, tenant.getGatewayAccountId());
            member.setGatewayCustomerId(customerId);
            memberRepository.save(member);
        }

        return stripeCheckoutService.createCheckoutSession(
                member, plan, tenant.getGatewayAccountId());
    }

    public void createFromWebhook(com.stripe.model.Subscription stripeSubscription,
                                  Tenant tenant) {
        if (subscriptionRepository.existsByGatewaySubscriptionId(
                stripeSubscription.getId())) return;

        String customerId = stripeSubscription.getCustomer();
        Member payer = memberRepository.findByGatewayCustomerId(customerId)
                .orElseThrow(() -> new IllegalStateException(
                        "No member found for customer: " + customerId));

        MembershipPlan plan = resolvePlanFromSubscription(stripeSubscription);

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(plan.getDurationValue());

        Subscription subscription = new Subscription();
        subscription.setPayer(payer);
        subscription.setMembershipPlan(plan);
        subscription.setGatewaySubscriptionId(stripeSubscription.getId());
        subscription.setGatewayStatus(stripeSubscription.getStatus());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(start);
        subscription.setEndDate(end);
        subscription.getMembers().add(payer);

        if (stripeSubscription.getItems() != null
                && !stripeSubscription.getItems().getData().isEmpty()) {
            SubscriptionItem item = stripeSubscription.getItems().getData().get(0);
            if (item.getCurrentPeriodStart() != null) {
                subscription.setGatewayCurrentPeriodStart(
                        Instant.ofEpochSecond(item.getCurrentPeriodStart())
                                .atZone(ZoneId.systemDefault()).toLocalDate());
            }
            if (item.getCurrentPeriodEnd() != null) {
                subscription.setGatewayCurrentPeriodEnd(
                        Instant.ofEpochSecond(item.getCurrentPeriodEnd())
                                .atZone(ZoneId.systemDefault()).toLocalDate());
            }
        }

        subscriptionRepository.save(subscription);
        log.info("Subscription created for member {}: {}", payer.getId(), subscription.getId());
    }

    public Subscription addBeneficiary(Integer subscriptionId, Integer beneficiaryMemberId,
                                       User user) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new NotFoundWithIdentifierException(
                        Subscription.class.getSimpleName(), subscriptionId
                ));

        Member payer = memberRepository.findByPersonUserId(user.getId())
                .orElseThrow(() -> new NotFoundWithIdentifierException(
                        Member.class.getSimpleName(), user.getId()
                ));

        if (!subscription.getPayer().getId().equals(payer.getId())) {
            throw new AccessDeniedException("Only the subscription payer can add beneficiaries");
        }

        int maxBeneficiaries = subscription.getMembershipPlan().getMaxBeneficiaries();
        if (subscription.getMembers().size() >= maxBeneficiaries) {
            throw new IllegalStateException(
                    "Plan allows a maximum of " + maxBeneficiaries + " members");
        }

        Member beneficiary = memberRepository.findById(beneficiaryMemberId)
                .orElseThrow(() -> new NotFoundWithIdentifierException(
                        Member.class.getSimpleName(), beneficiaryMemberId
                ));

        subscription.getMembers().add(beneficiary);
        return subscriptionRepository.save(subscription);
    }

    public Subscription removeBeneficiary(Integer subscriptionId, Integer beneficiaryMemberId,
                                          User user) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new NotFoundWithIdentifierException(
                        Subscription.class.getSimpleName(), subscriptionId
                ));

        Member payer = memberRepository.findByPersonUserId(user.getId())
                .orElseThrow(() -> new NotFoundWithIdentifierException(
                        Member.class.getSimpleName(), user.getId()
                ));

        if (!subscription.getPayer().getId().equals(payer.getId())) {
            throw new AccessDeniedException("Only the subscription payer can remove beneficiaries");
        }

        Member beneficiary = memberRepository.findById(beneficiaryMemberId)
                .orElseThrow(() -> new NotFoundWithIdentifierException(
                        Member.class.getSimpleName(), beneficiaryMemberId
                ));

        if (beneficiary.getId().equals(payer.getId())) {
            throw new IllegalStateException("Cannot remove the payer from their own subscription");
        }

        subscription.getMembers().remove(beneficiary);
        return subscriptionRepository.save(subscription);
    }

    @Transactional(readOnly = true)
    public Subscription findActiveByUser(User user) {
        Member member = memberRepository.findByPersonUserId(user.getId())
                .orElseThrow(() -> new NotFoundWithIdentifierException(
                        Member.class.getSimpleName(), user.getId()
                ));

        return subscriptionRepository
                .findTopByPayerIdAndStatusOrderByStartDateDesc(
                        member.getId(), SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundWithIdentifierException(
                        Subscription.class.getSimpleName(), member.getId()
                ));
    }

    private MembershipPlan resolvePlanFromSubscription(
            com.stripe.model.Subscription stripeSubscription) {
        if (stripeSubscription.getItems() == null
                || stripeSubscription.getItems().getData().isEmpty()) {
            throw new IllegalStateException("Subscription has no items");
        }
        String priceId = stripeSubscription.getItems().getData().getFirst().getPrice().getId();
        return membershipPlanRepository.findByGatewayPriceId(priceId)
                .orElseThrow(() -> new IllegalStateException(
                        "No plan found for price: " + priceId));
    }

    @Transactional(readOnly = true)
    public SubscriptionResponseDTO findActiveByUserAsDto(User user) {
        return subscriptionMapper.toDto(findActiveByUser(user));
    }

    public SubscriptionResponseDTO addBeneficiaryAsDto(
            Integer subscriptionId,
            Integer memberId,
            User user
    ) {
        return subscriptionMapper.toDto(addBeneficiary(
                subscriptionId, memberId, user
        ));
    }

    public List<SubscriptionAdminResponseDTO> findAllAsDto() {
        return subscriptionRepository.findAll().stream()
                .map(this::toAdminDto)
                .toList();
    }

    public List<SubscriptionAdminResponseDTO> findByMemberAsDto(Integer memberId) {
        return subscriptionRepository.findByPayerId(memberId).stream()
                .map(this::toAdminDto)
                .toList();
    }

    private SubscriptionAdminResponseDTO toAdminDto(Subscription subscription) {
        List<PaymentResponseDTO> payments = paymentRepository
                .findBySubscriptionId(subscription.getId()).stream()
                .map(paymentMapper::toDto)
                .toList();
        return new SubscriptionAdminResponseDTO(
                subscription.getId(),
                subscription.getStatus(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.getGatewayCurrentPeriodStart(),
                subscription.getGatewayCurrentPeriodEnd(),
                membershipPlanMapper.toSummaryDto(subscription.getMembershipPlan()),
                subscription.getMembers().stream()
                        .map(m -> new SubscriptionMemberDTO(
                                m.getId(),
                                m.getPerson().getName(),
                                m.getPerson().getLastName()
                        ))
                        .collect(Collectors.toSet()),
                payments
        );
    }
}
