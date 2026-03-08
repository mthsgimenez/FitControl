package com.mthsgimenez.fitcontrol.member;

import com.mthsgimenez.fitcontrol.person.Person;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "members")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Column(name = "goal", nullable = false)
    private String goal;

    @Column(name = "training_level", nullable = false)
    @Enumerated(EnumType.STRING)
    private TrainingLevel trainingLevel;

    @Column(name = "restrictions")
    private String restrictions;

    @Column(name = "gateway_customer_id")
    private String gatewayCustomerId;
}