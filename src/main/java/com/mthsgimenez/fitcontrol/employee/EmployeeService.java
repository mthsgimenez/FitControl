package com.mthsgimenez.fitcontrol.employee;

import com.mthsgimenez.fitcontrol.infra.exception.FKConstraintViolationException;
import com.mthsgimenez.fitcontrol.infra.exception.NotFoundWithIdentifierException;
import com.mthsgimenez.fitcontrol.person.Person;
import com.mthsgimenez.fitcontrol.user.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final RoleService roleService;
    private final UserRepository userRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            RoleService roleService,
            UserRepository userRepository,
            EmployeeMapper employeeMapper
    ) {
        this.employeeRepository = employeeRepository;
        this.roleService = roleService;
        this.userRepository = userRepository;
        this.employeeMapper = employeeMapper;
    }

    public Employee createEmployee(EmployeeDTO data) {
        Employee newEmployee = new Employee();
        newEmployee.setAdmissionDate(data.admissionDate());
        newEmployee.setPerson(data.person());

        return employeeRepository.save(newEmployee);
    }

    public EmployeeResponseDTO createEmployeeAsDto(EmployeeDTO data) {
        Employee createdEmployee = createEmployee(data);
        return employeeMapper.toResponseDTO(createdEmployee);
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
        currentRoles.removeIf(role -> {
            RoleType enumRole = roleService.entityToEnum(role);
            return RoleType.EMPLOYEE_ROLES.contains(enumRole);
        });
        currentRoles.addAll(newRolesEntities);

        employeeUser.setRoles(currentRoles);
        userRepository.save(employeeUser);

        return employee;
    }

    public EmployeeResponseDTO editEmployeeRolesAsDto(Integer employeeId, Set<RoleType> roles) {
        Employee updatedEmployee = editEmployeeRoles(employeeId, roles);
        return employeeMapper.toResponseDTO(updatedEmployee);
    }

    @Transactional(readOnly = true)
    public Employee findById(Integer employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundWithIdentifierException(Employee.class.getSimpleName(), employeeId));
    }

    public EmployeeResponseDTO findByIdAsDto(Integer employeeId) {
        Employee employee = findById(employeeId);
        return employeeMapper.toResponseDTO(employee);
    }

    @Transactional(readOnly = true)
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> findAllAsDto() {
        List<Employee> employees = findAll();
        return employees.stream()
                .map(employeeMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public void deleteById(Integer employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundWithIdentifierException(Employee.class.getSimpleName(), employeeId));
        Person person = employee.getPerson();
        User user = person.getUser();
        user.getRoles().removeIf(role -> {
            RoleType enumRole = roleService.entityToEnum(role);
            return RoleType.EMPLOYEE_ROLES.contains(enumRole);
        });

        try {
            employeeRepository.delete(employee);
        } catch (DataIntegrityViolationException e) {
            throw new FKConstraintViolationException(Employee.class.getSimpleName(), employeeId);
        }
    }
}