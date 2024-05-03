package uz.pdp.rest_api_jwt.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import uz.pdp.rest_api_jwt.config.SecurityConfig;
import uz.pdp.rest_api_jwt.entity.Employee;
import uz.pdp.rest_api_jwt.payload.TaskDto;
import uz.pdp.rest_api_jwt.repository.EmployeeTaskRepository;
import uz.pdp.rest_api_jwt.service.ApiResponse;
import uz.pdp.rest_api_jwt.service.EmployeeTaskService;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@RestController
@RequestMapping("/api/employeeTask")
public class EmployeeTaskController {

    @Autowired
    EmployeeTaskRepository employeeTaskRepository;

    @Autowired
    EmployeeTaskService employeeTaskService;

    @Autowired
    SecurityConfig securityConfig;


    // TASKS FOR AN EMPLOYEE LOGGED INTO THE SYSTEM
    @PreAuthorize(value ="hasAnyRole('EMPLOYEE')")
    @GetMapping("/tasks")
    public HttpEntity<?>getTaskById(){
    Set<Integer> set=new HashSet<>(Arrays.asList(1,2));
    return  ResponseEntity.ok(employeeTaskRepository.findByEmployeeIdAndStatusIn(getEmployeeById(),set));
    }

    // AN EMPLOYEE LOGGED INTO THE SYSTEM
    public UUID getEmployeeById(){
        Employee principal = (Employee) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getId();
    }


    @PreAuthorize(value ="hasAnyRole('MANAGER','DIRECTOR')")     // ROLE ID of AN EMPLOYEE LOGGED INTO THE SYSTEM  4
    @GetMapping
    public HttpEntity<?>getTasks(){
        return ResponseEntity.ok(employeeTaskRepository.findAllByEmployee_RolesId(4));
    }


    @PreAuthorize(value ="hasAnyRole('MANAGER','DIRECTOR')")
    @PostMapping
    public HttpEntity<?>addTask(@RequestBody TaskDto taskDto){
        ApiResponse apiResponse = employeeTaskService.addTask(taskDto);
        return ResponseEntity.status(apiResponse.isSuccess()?200:409).body(apiResponse);
    }


    @PreAuthorize(value ="hasAnyRole('MANAGER','DIRECTOR')")     // IF THE ROLE ID OF THE TASK'S EMPLOYEE IS 4
    @PutMapping("/{id}")
    public HttpEntity<?>editTask(@PathVariable UUID id, @RequestBody TaskDto taskDto){
        ApiResponse apiResponse = employeeTaskService.editTask(id,taskDto);
        return ResponseEntity.status(apiResponse.isSuccess()?200:409).body(apiResponse);
    }


    @PreAuthorize(value ="hasAnyRole('MANAGER','DIRECTOR')")    // IF THE ROLE ID OF THE TASK'S EMPLOYEE IS 4
    @DeleteMapping("/{id}")
    public HttpEntity<?>deleteTask(@PathVariable UUID id){
        ApiResponse apiResponse = employeeTaskService.deleteTask(id);
        return ResponseEntity.status(apiResponse.isSuccess()?200:409).body(apiResponse);
    }


}



