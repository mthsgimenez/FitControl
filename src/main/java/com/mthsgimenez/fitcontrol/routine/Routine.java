package com.mthsgimenez.fitcontrol.routine;

import com.mthsgimenez.fitcontrol.employee.Employee;
import com.mthsgimenez.fitcontrol.member.Member;
import com.mthsgimenez.fitcontrol.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "routines", uniqueConstraints = {
        @UniqueConstraint(name = "routines_name_member_id_key", columnNames = {"name", "member_id"})
})
public class Routine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(
            mappedBy = "routine",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("dayOrder ASC")
    private Set<RoutineDay> days = new LinkedHashSet<>();

    public void addDay(RoutineDay day) {
        days.add(day);
        day.setRoutine(this);
    }

    public void removeDay(RoutineDay day) {
        days.remove(day);
        day.setRoutine(null);
    }

    public void clearDays() {
        for (RoutineDay day : days) {
            day.setRoutine(null);
        }
        days.clear();
    }
}
