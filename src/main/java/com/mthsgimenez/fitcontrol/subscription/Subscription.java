package com.mthsgimenez.fitcontrol.subscription;

import com.mthsgimenez.fitcontrol.member.Member;
import com.mthsgimenez.fitcontrol.membershipplan.MembershipPlan;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membership_plan_id", nullable = false)
    private MembershipPlan membershipPlan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payer_id", nullable = false)
    private Member payer;

    @Column(name = "gateway_subscription_id")
    private String gatewaySubscriptionId;

    @Column(name = "gateway_status")
    private String gatewayStatus;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "gateway_current_period_start")
    private LocalDate gatewayCurrentPeriodStart;

    @Column(name = "gateway_current_period_end")
    private LocalDate gatewayCurrentPeriodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status;

    @ManyToMany
    @JoinTable(
            name = "subscription_members",
            joinColumns = @JoinColumn(name = "subscription_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"subscription_id", "member_id"})
    )
    private Set<Member> members = new HashSet<>();
}
