package com.mthsgimenez.fitcontrol.employee;

import com.mthsgimenez.fitcontrol.person.PersonService;
import com.mthsgimenez.fitcontrol.user.UserService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Employee createEmployee(EmployeeDTO data) {
        Employee newEmployee = new Employee();
        newEmployee.setAdmissionDate(data.admissionDate());
        newEmployee.setPerson(data.person());

        return employeeRepository.save(newEmployee);
    }
}
