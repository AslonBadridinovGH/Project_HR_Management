
package uz.pdp.rest_api_jwt.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.rest_api_jwt.payload.LoginDto;
import uz.pdp.rest_api_jwt.payload.RegisterDto;
import uz.pdp.rest_api_jwt.repository.EmployeeRepository;
import uz.pdp.rest_api_jwt.repository.RoleRepository;
import uz.pdp.rest_api_jwt.service.ApiResponse;
import uz.pdp.rest_api_jwt.service.EmployeeService;
import java.util.UUID;


@RestController
@RequestMapping("/api/auth")
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    RoleRepository roleRepository;

    @PreAuthorize(value = "hasRole('HR_MANAGER')")
    @PostMapping("/register/employee")
    public HttpEntity<?> registerEmployee(@RequestBody RegisterDto registerDto) {
        ApiResponse apiResponse = employeeService.registerEmployee(registerDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 201 : 409).body(apiResponse);
    }

    @PostMapping(value = "/login/employee")
    public HttpEntity<?> loginEmployee(@RequestBody LoginDto loginDto) {
        ApiResponse apiResponse = employeeService.loginEmployee(loginDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 401).body(apiResponse);
    }

    // if click  Confirm LINK in EMAIL, act this METHOD  and take out email and emailCode   from LINK .
    @PostMapping(value = "/verifyEmail/employee")
    public HttpEntity<?> employeeVerifyEmail(@RequestParam String emailCode, @RequestParam String email, @RequestParam String password) {
        ApiResponse apiResponse = employeeService.employeeVerifyEmail(emailCode, email, password);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }


    // List of Employee.
    @PreAuthorize(value = "hasAnyRole('DIRECTOR','HRMANAGER')")
    @GetMapping("/getEmployees")
    public HttpEntity<?> getEmployees() {
        return ResponseEntity.ok(employeeRepository.findAll());
    }


    @PreAuthorize(value = "hasAnyRole('DIRECTOR','HRMANAGER')")
    @GetMapping("/{id}/{date1}/{date2}")
    public HttpEntity<?> getEmployeeById(@PathVariable UUID id, @PathVariable String date1, @PathVariable String date2) {
        ApiResponse apiResponse = employeeService.getEmployeeByIdTimeStatus(id, date1, date2);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }


    //  Tasks of Employee
    @PreAuthorize(value = "hasAnyRole('DIRECTOR','HRMANAGER')")
    @GetMapping("/{id}/{localDate1}/{localDate2}")
    public HttpEntity<?> getEmployeeById1(@PathVariable UUID id, @PathVariable String localDate1, @PathVariable String localDate2) {
        ApiResponse apiResponse = employeeService.getEmployeeByIdTimeStatus1(id, localDate1, localDate2);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

}




/*      Set<Role> roleSet=new HashSet<>();
        roleSet.add(roleRepository.findByRoleName(EMPLOYEE));
        roleSet.add(roleRepository.findByRoleName(MANAGER));
        return ResponseEntity.ok(employeeRepository.findByRoles(roleSet));
        ApiResponse apiResponse1 = employeeService.getEmployees();
        return ResponseEntity.status(apiResponse.isSuccess()?200:409).body(apiResponse);
*/
