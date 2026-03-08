package com.mthsgimenez.fitcontrol.paymentgateway;

import com.mthsgimenez.fitcontrol.member.Member;
import com.mthsgimenez.fitcontrol.person.Person;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StripeCustomerService {

    public String createCustomer(Member member, String connectedAccountId) {
        try {
            Person person = member.getPerson();
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setName(person.getName() + " " + person.getLastName())
                    .putMetadata("member_id", member.getId().toString())
                    .build();

            RequestOptions options = RequestOptions.builder()
                    .setStripeAccount(connectedAccountId)
                    .build();

            Customer customer = Customer.create(params, options);
            log.info("Stripe customer created for member {}: {}", member.getId(), customer.getId());
            return customer.getId();
        } catch (StripeException e) {
            log.error("Failed to create Stripe customer for member {}", member.getId(), e);
            throw new PaymentGatewayException("Customer creation failed", e);
        }
    }
}
