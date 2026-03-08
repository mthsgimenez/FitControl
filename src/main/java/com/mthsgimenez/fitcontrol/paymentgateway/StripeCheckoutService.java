package com.mthsgimenez.fitcontrol.paymentgateway;

import com.mthsgimenez.fitcontrol.member.Member;
import com.mthsgimenez.fitcontrol.membershipplan.MembershipPlan;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StripeCheckoutService {

    @Value("${app.gateway.checkout.success-url}")
    private String successUrl;

    @Value("${app.gateway.checkout.cancel-url}")
    private String cancelUrl;

    public String createCheckoutSession(
            Member member,
            MembershipPlan plan,
            String connectedAccountId) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setCustomer(member.getGatewayCustomerId())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPrice(plan.getGatewayPriceId())
                                    .setQuantity(1L)
                                    .build()
                    )
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .putMetadata("member_id", member.getId().toString())
                    .putMetadata("plan_id", plan.getId().toString())
                    .build();

            RequestOptions options = RequestOptions.builder()
                    .setStripeAccount(connectedAccountId)
                    .build();

            Session session = Session.create(params, options);
            log.info("Checkout session created for member {}: {}", member.getId(), session.getId());
            return session.getUrl();
        } catch (StripeException e) {
            log.error("Failed to create checkout session for member {}", member.getId(), e);
            throw new PaymentGatewayException("Checkout session creation failed", e);
        }
    }
}