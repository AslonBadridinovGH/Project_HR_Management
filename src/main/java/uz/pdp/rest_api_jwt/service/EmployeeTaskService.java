package uz.pdp.rest_api_jwt.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import uz.pdp.rest_api_jwt.entity.Employee;
import uz.pdp.rest_api_jwt.entity.Role;
import uz.pdp.rest_api_jwt.entity.Task;
import uz.pdp.rest_api_jwt.payload.TaskDto;
import uz.pdp.rest_api_jwt.repository.*;

import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class EmployeeTaskService {

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    EmployeeTaskRepository employeeTaskRepository;

    @Autowired
    JavaMailSender javaMailSender;


    public static final  Integer NEW =1;
    public static final  Integer AT_PROCESS =2;
    public static final  Integer DONE_ON_TIME=3;
    public static final  Integer LATE_DONE=4;


    public ApiResponse addTask(TaskDto taskDto){

         Task task=new Task();
        task.setTaskName(taskDto.getTaskName());
        task.setDescription(taskDto.getDescription());
        task.setTaskCode(taskDto.getTaskCode());
        task.setDeadLine(taskDto.getDeadLine());
        task.setStatus(NEW);

        if (taskDto.getEmployeeId()!=null)
        {
            Optional<Employee> optionalEmployee = employeeRepository.findById(taskDto.getEmployeeId());
            if (!optionalEmployee.isPresent())
            return new ApiResponse("Employee not found", false);

            Employee employee = optionalEmployee.get();
            Set<Role> roles = employee.getRoles();
            for (Role role : roles) {
                if (!role.getRoleName().toString().equals("EMPLOYEE")){
                    return new ApiResponse("Enter a different Employee ID", false);
                }
            }
            task.setEmployee(employee);
            sendEMail(employee.getEmail());
            task.setEnabled(true);
            task.setStatus(AT_PROCESS);
        }
        employeeTaskRepository.save(task);
        return new ApiResponse("saved successfully",true);
    }


    public ApiResponse editTask(UUID id, TaskDto taskDto){

        Optional<Task> optionalTask = employeeTaskRepository.findByIdAndEmployeeRolesId(id,4);
        if (!optionalTask.isPresent())
            return new ApiResponse("Task not found", false);
         Task task = optionalTask.get();
        task.setTaskName(taskDto.getTaskName());
        task.setDescription(taskDto.getDescription());
        task.setTaskCode(taskDto.getTaskCode());
        task.setDeadLine(taskDto.getDeadLine());
        task.setCompletedAt(taskDto.getCompletedAt());

        if (task.getCompletedAt()!=null) {

            if (Period.between(task.getDeadLine(), task.getCompletedAt()).getDays() > 0) {
                task.setStatus(LATE_DONE);

                UUID createdBy = task.getCreatedBy();
                Optional<Employee> employeeOp = employeeRepository.findById(createdBy);
                if (employeeOp.isPresent()) {
                    Employee manager = employeeOp.get();
                    sendEMail1(manager.getEmail());
                }
            } else if (Period.between(task.getDeadLine(), task.getCompletedAt()).getDays() <= 0) {
                task.setStatus(DONE_ON_TIME);
                UUID createdBy = task.getCreatedBy();
                Optional<Employee> employeeOp = employeeRepository.findById(createdBy);
                if (employeeOp.isPresent()) {
                    Employee manager = employeeOp.get();
                    sendEMail1(manager.getEmail());
                }
            }
        }

        if (taskDto.getEmployeeId()!=null) {
            Optional<Employee> optionalEmployee = employeeRepository.findById(taskDto.getEmployeeId());
            if (!optionalEmployee.isPresent())
                return new ApiResponse("Employee not found", false);

            Employee employee = optionalEmployee.get();
            task.setEmployee(employee);
            sendEMail(employee.getEmail());
        }
        employeeTaskRepository.save(task);
        return new ApiResponse("Changed successfully",true);

    }

    public ApiResponse deleteTask(UUID id) {
        Optional<Task> optionalTask = employeeTaskRepository.findByIdAndEmployeeRolesId(id,4);
        if (optionalTask.isPresent()){
            try {
                employeeTaskRepository.deleteById(id);
                return new ApiResponse("Task deleted", true);
            } catch (Exception e) {
                return new ApiResponse("Task not deleted", false);
            }}
        return new ApiResponse("Task not found", false);
    }

    public Boolean sendEMail(String sendingEmail){
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom("aslon.dinov@gmail.com");
            mailMessage.setTo(sendingEmail);
            mailMessage.setSubject("new task !!!");
            mailMessage.setText("you have new task");
            javaMailSender.send(mailMessage);
            return true;
        }catch (Exception e){
            return false;
        }
    }
    public Boolean sendEMail1(String sendingEmail){
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom("aslon.dinov@gmail.com");
            mailMessage.setTo(sendingEmail);
            mailMessage.setSubject("TASK");
            mailMessage.setText("task failed");
            javaMailSender.send(mailMessage);
            return true;

        }catch (Exception e){
            return false;
        }
    }
}

