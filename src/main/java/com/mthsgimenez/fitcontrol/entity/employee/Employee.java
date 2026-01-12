package com.mthsgimenez.fitcontrol.entity.employee;

import com.mthsgimenez.fitcontrol.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "employees", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "employees_cpf_key",
                columnNames = {"cpf"}),
        @UniqueConstraint(name = "employees_user_id_key",
                columnNames = {"user_id"})})
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "cpf", nullable = false, length = 11)
    private String cpf;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


}