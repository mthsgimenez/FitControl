package com.mthsgimenez.fitcontrol.paymentgateway;

import com.mthsgimenez.fitcontrol.membershipplan.MembershipPlan;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.net.RequestOptions;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.PriceUpdateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class StripePriceService {

    public String createPrice(MembershipPlan plan, String productId, String connectedAccountId) {
        try {
            PriceCreateParams params = PriceCreateParams.builder()
                    .setProduct(productId)
                    .setCurrency("brl")
                    .setUnitAmount(plan.getPrice()
                            .multiply(BigDecimal.valueOf(100))
                            .longValue())
                    .setRecurring(
                            PriceCreateParams.Recurring.builder()
                                    .setInterval(PriceCreateParams.Recurring.Interval.MONTH)
                                    .setIntervalCount(Long.valueOf(plan.getDurationValue()))
                                    .build()
                    )
                    .setNickname(plan.getName())
                    .putMetadata("plan_name", plan.getName())
                    .build();

            RequestOptions options = RequestOptions.builder()
                    .setStripeAccount(connectedAccountId)
                    .build();

            Price price = Price.create(params, options);
            log.info("Stripe price created: {} for plan: {}", price.getId(), plan.getName());
            return price.getId();

        } catch (StripeException e) {
            log.error("Failed to create Stripe price for plan: {}", plan.getName(), e);
            throw new PaymentGatewayException("Price creation failed", e);
        }
    }

    public void archivePrice(String priceId, String connectedAccountId) {
        try {
            Price price = Price.retrieve(priceId,
                    RequestOptions.builder().setStripeAccount(connectedAccountId).build());

            price.update(
                    PriceUpdateParams.builder().setActive(false).build(),
                    RequestOptions.builder().setStripeAccount(connectedAccountId).build()
            );
            log.info("Stripe price archived: {}", priceId);

        } catch (StripeException e) {
            log.error("Failed to archive Stripe price: {}", priceId, e);
            throw new PaymentGatewayException("Price archival failed", e);
        }
    }
}
