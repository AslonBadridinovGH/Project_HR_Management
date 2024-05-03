package uz.pdp.rest_api_jwt.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.rest_api_jwt.entity.Employee;
import uz.pdp.rest_api_jwt.entity.Salary;
import uz.pdp.rest_api_jwt.payload.SalaryDto;
import uz.pdp.rest_api_jwt.repository.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SalaryService {

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    SalaryRepository salaryRepository;


    public ApiResponse addSalary(SalaryDto salaryDto){

          Salary salary=new Salary();
        salary.setMonth(salaryDto.getMonth());
        salary.setVerifyingCode(salaryDto.getVerifyingCode());
        salary.setLocalDate(salaryDto.getLocalDate());
        Optional<Employee> employee = employeeRepository.findById(salaryDto.getEmployeeId());
        if (!employee.isPresent())
        return new ApiResponse("Employee not found",false);
        salary.setEmployee(employee.get());
        salaryRepository.save(salary);
        return new ApiResponse("successfully saved",true);
    }

    public ApiResponse editSalary(UUID id, SalaryDto salaryDto) {
        Optional<Salary> optionalSalary = salaryRepository.findById(id);
        if (optionalSalary.isPresent()) {
             Salary editingSalary = optionalSalary.get();
            editingSalary.setMonth(salaryDto.getMonth());
            editingSalary.setLocalDate(salaryDto.getLocalDate());
            editingSalary.setVerifyingCode(salaryDto.getVerifyingCode());

            Optional<Employee> employee = employeeRepository.findById(salaryDto.getEmployeeId());
            if (!employee.isPresent())
                return new ApiResponse("Employee not found", false);
            editingSalary.setEmployee(employee.get());
            salaryRepository.save(editingSalary);
        }
        return new ApiResponse(" not saved", false);
    }

    public ApiResponse getSalary(UUID id) {

        Optional<Salary> optionalSalary = salaryRepository.findById(id);
        return optionalSalary.map(employeeSalary -> new ApiResponse("Salary not found", true, employeeSalary)).orElseGet(() -> new ApiResponse("Salary found", false));
    }

    public ApiResponse deleteSalary(UUID id) {

        Optional<Salary> optionalSalary = salaryRepository.findById(id);
        if (optionalSalary.isPresent())
            try {
                salaryRepository.deleteById(id);
                return new ApiResponse("Salary deleted", true);
            } catch (Exception e) {
                return new ApiResponse("Salary not found", false);
            }
        return new ApiResponse("Salary not deleted", false);
    }

    public ApiResponse getSalaryByEmployeeId(UUID id) {
        List<Salary> byEmployeeId = salaryRepository.findAllByEmployeeId(id);
        return new ApiResponse("Salaries",true, byEmployeeId);
    }

    public ApiResponse getSalaryByMonth(String month) {
        List<Salary> byMonth = salaryRepository.findAllByMonth(month);
        return new ApiResponse("Salaries",true, byMonth);
    }

}

