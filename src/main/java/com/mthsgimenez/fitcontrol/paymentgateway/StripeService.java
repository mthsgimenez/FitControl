package com.mthsgimenez.fitcontrol.paymentgateway;

import com.mthsgimenez.fitcontrol.tenant.Tenant;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.Product;
import com.stripe.net.RequestOptions;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.ProductCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StripeService {

    public StripeService(@Value("${app.gateway.secret-key}") String stripeApiKey) {
        Stripe.apiKey = stripeApiKey;
    }

    public String createConnectedAccount(Tenant tenant, String email) {
        try {
            AccountCreateParams params = AccountCreateParams.builder()
                    .setType(AccountCreateParams.Type.EXPRESS)
                    .setCountry("BR")
                    .setEmail(email)
                    .setBusinessType(AccountCreateParams.BusinessType.COMPANY)
                    .setBusinessProfile(
                            AccountCreateParams.BusinessProfile.builder()
                                    .setName(tenant.getTradeName() != null
                                            ? tenant.getTradeName()
                                            : tenant.getLegalName())
                                    .build()
                    )
                    .putMetadata("tenant_uuid", tenant.getUuid().toString())
                    .setCapabilities(
                            AccountCreateParams.Capabilities.builder()
                                    .setCardPayments(
                                            AccountCreateParams.Capabilities.CardPayments.builder()
                                                    .setRequested(true).build())
                                    .setTransfers(
                                            AccountCreateParams.Capabilities.Transfers.builder()
                                                    .setRequested(true).build())
                                    .build()
                    )
                    .build();

            Account account = Account.create(params);
            log.info("Stripe connected account created for tenant {}: {}",
                    tenant.getUuid(), account.getId());
            return account.getId();
        } catch (StripeException e) {
            log.error("Failed to create Stripe connected account for tenant {}",
                    tenant.getUuid(), e);
            throw new PaymentGatewayException("Connected account creation failed", e);
        }
    }

    public String createAccountOnboardingLink(String connectedAccountId,
                                              String refreshUrl,
                                              String returnUrl) {
        try {
            AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                    .setAccount(connectedAccountId)
                    .setRefreshUrl(refreshUrl)
                    .setReturnUrl(returnUrl)
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .build();

            AccountLink link = AccountLink.create(params);
            return link.getUrl();
        } catch (StripeException e) {
            log.error("Failed to create onboarding link for account {}", connectedAccountId, e);
            throw new PaymentGatewayException("Onboarding link creation failed", e);
        }
    }

    public String createProduct(Tenant tenant) {
        try {
            ProductCreateParams params = ProductCreateParams.builder()
                    .setName("Gym Membership")
                    .putMetadata("tenant_uuid", tenant.getUuid().toString())
                    .build();

            RequestOptions options = RequestOptions.builder()
                    .setStripeAccount(tenant.getGatewayAccountId())
                    .build();

            Product product = Product.create(params, options);
            log.info("Stripe product created on connected account for tenant {}: {}",
                    tenant.getUuid(), product.getId());
            return product.getId();
        } catch (StripeException e) {
            log.error("Failed to create Stripe product for tenant {}", tenant.getUuid(), e);
            throw new PaymentGatewayException("Product creation failed", e);
        }
    }
}
