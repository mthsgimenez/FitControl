package com.mthsgimenez.fitcontrol.employee;

import com.mthsgimenez.fitcontrol.infra.exception.FKConstraintViolationException;
import com.mthsgimenez.fitcontrol.infra.exception.NotFoundWithIdentifierException;
import com.mthsgimenez.fitcontrol.person.Person;
import com.mthsgimenez.fitcontrol.user.*;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final RoleService roleService;
    private final UserRepository userRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           RoleService roleService,
                           UserRepository userRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.roleService = roleService;
        this.userRepository = userRepository;
    }

    public Employee createEmployee(EmployeeDTO data) {
        Employee newEmployee = new Employee();
        newEmployee.setAdmissionDate(data.admissionDate());
        newEmployee.setPerson(data.person());

        return employeeRepository.save(newEmployee);
    }

    public Employee editEmployeeRoles(Integer employeeId, Set<RoleType> roles) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundWithIdentifierException(Employee.class.getSimpleName(), employeeId));
        User employeeUser = employee.getPerson().getUser();

        Set<RoleType> newRolesEnum;
        if (roles == null || roles.isEmpty()) {
            newRolesEnum = EnumSet.noneOf(RoleType.class);
        } else {
            newRolesEnum = EnumSet.copyOf(roles);
        }

        newRolesEnum.retainAll(RoleType.EMPLOYEE_ROLES);
        Set<Role> newRolesEntities = newRolesEnum.stream().map(roleService::enumToEntity).collect(Collectors.toSet());
        Set<Role> currentRoles = employeeUser.getRoles();
        currentRoles.removeIf(
                role -> {
                    RoleType enumRole = roleService.entityToEnum(role);
                    return RoleType.EMPLOYEE_ROLES.contains(enumRole);
                }
        );
        currentRoles.addAll(newRolesEntities);

        employeeUser.setRoles(currentRoles);
        userRepository.save(employeeUser);

        return employee;
    }

    public Employee findById(Integer employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundWithIdentifierException(Employee.class.getSimpleName(), employeeId));
    }

    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Transactional
    public void deleteById(Integer employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundWithIdentifierException(Employee.class.getSimpleName(), employeeId));
        Person person = employee.getPerson();
        User user = person.getUser();
        user.getRoles().removeIf(role -> {
            RoleType enumRole = roleService.entityToEnum(role);
            return RoleType.EMPLOYEE_ROLES.contains(enumRole);
        });

        person.setEmployee(null);

        try {
            employeeRepository.delete(employee);
        } catch (DataIntegrityViolationException e) {
            throw new FKConstraintViolationException(Employee.class.getSimpleName(), employeeId);
        }
    }
}
