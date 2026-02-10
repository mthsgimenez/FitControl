package com.mthsgimenez.fitcontrol.person.repository;

import com.mthsgimenez.fitcontrol.person.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Integer> {
}
