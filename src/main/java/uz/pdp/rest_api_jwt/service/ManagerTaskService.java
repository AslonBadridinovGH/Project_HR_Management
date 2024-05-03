package uz.pdp.rest_api_jwt.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import uz.pdp.rest_api_jwt.entity.Employee;
import uz.pdp.rest_api_jwt.entity.Role;
import uz.pdp.rest_api_jwt.entity.Task;
import uz.pdp.rest_api_jwt.payload.TaskDto;
import uz.pdp.rest_api_jwt.repository.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ManagerTaskService {

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    ManagerTaskRepository managerTaskRepository;

    @Autowired
    JavaMailSender javaMailSender;


    public static final  Integer NEW =1;
    public static final  Integer AT_PROCESS =2;
    public static final  Integer DONE_ON_Time=3;
    public static final  Integer LATE_DONE=4;


    public ApiResponse addTask(TaskDto taskDto){

          Task task=new Task();
        task.setTaskName(taskDto.getTaskName());
        task.setDescription(taskDto.getDescription());
        task.setTaskCode(taskDto.getTaskCode());
        task.setDeadLine(taskDto.getDeadLine());
        task.setStatus(NEW);

       // ASSIGNS TASK TO MANAGER
        if (taskDto.getEmployeeId() != null)
         {
           Optional<Employee> optionalManager = employeeRepository.findById(taskDto.getEmployeeId());
           if (!optionalManager.isPresent())
           return new ApiResponse("Manager not found ", false);

             Employee manager = optionalManager.get();
             for (Role role : manager.getRoles()) {
                if (!role.getRoleName().toString().equals("MANAGER")){
                return new ApiResponse("Please choose another id ", false);
                }
           }
             task.setEmployee(manager);
             sendEMail(manager.getEmail());
             task.setEnabled(true);
             task.setStatus(AT_PROCESS);
/*
 Set<Task> tasks = manager.getTask();
 tasks.add(task);
*/
        }
        managerTaskRepository.save(task);
        return new ApiResponse("Saved successfully",true);
    }

    public ApiResponse editTask(UUID id, TaskDto taskDto) {

        Optional<Task> optionalTask = managerTaskRepository.findByIdAndEmployeeRolesId(id,3);
        if (!optionalTask.isPresent())
            return new ApiResponse("Task not found", false);

           Task task = optionalTask.get();
        task.setTaskName(taskDto.getTaskName());
        task.setDescription(taskDto.getDescription());
        task.setTaskCode(taskDto.getTaskCode());
        task.setDeadLine(taskDto.getDeadLine());
        task.setCompletedAt(taskDto.getCompletedAt());

        if (task.getCompletedAt()!=null){
            if (Period.between(task.getDeadLine(), task.getCompletedAt()).getDays() > 0) {
                task.setStatus(LATE_DONE);
                UUID createdBy = task.getCreatedBy();
                Optional<Employee> employeeOp = employeeRepository.findById(createdBy);
                if (employeeOp.isPresent()) {
                    Employee manager = employeeOp.get();
                    sendEMail1(manager.getEmail());
                }
            } else if (Period.between(task.getDeadLine(), task.getCompletedAt()).getDays() <= 0) {
                task.setStatus(DONE_ON_Time);

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
                return new ApiResponse("Manager not found", false);

            // TASK ATTACHED TO MANAGER
             Employee employee = optionalEmployee.get();
            task.setEmployee(employee);
            sendEMail(employee.getEmail());
        }

        managerTaskRepository.save(task);
        return new ApiResponse("Changed successfully",true);
    }

    public ApiResponse getTaskById(UUID id) {
        Optional<Task> optionalTask = managerTaskRepository.findByIdAndEmployeeRolesId(id,3);
        if (!optionalTask.isPresent()){
           return new ApiResponse("Task not found",false);
        }
    return new ApiResponse("Task",true, optionalTask.get());
    // return optionalTask.map(task -> new ApiResponse("Task found", true, task)).orElseGet(() -> new ApiResponse("Task not found", false));
    }

    public ApiResponse deleteTask(UUID id) {

        Optional<Task> optionalTask = managerTaskRepository.findByIdAndEmployeeRolesId(id,3);
        if (optionalTask.isPresent()){
            try {
                managerTaskRepository.deleteById(id);
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
            mailMessage.setSubject("NEW TASK");
            mailMessage.setText("you have new Task");
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
            mailMessage.setText("Task done");
            javaMailSender.send(mailMessage);
            return true;

        }catch (Exception e){
            return false;
        }
    }

}
