package com.mthsgimenez.fitcontrol.membership;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "membership_plans", uniqueConstraints = {
        @UniqueConstraint(name = "membership_plans_name_key", columnNames = {"name"})
})
public class MembershipPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_value", nullable = false)
    private Integer durationValue;

    @Column(name = "max_beneficiaries")
    private Integer maxBeneficiaries = 1;

    @Column(name = "gateway_price_id")
    private String gatewayPriceId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
