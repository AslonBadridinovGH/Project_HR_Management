

package uz.pdp.rest_api_jwt.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.pdp.rest_api_jwt.entity.Employee;
import uz.pdp.rest_api_jwt.entity.Task;
import uz.pdp.rest_api_jwt.entity.TourniquetCard;
import uz.pdp.rest_api_jwt.entity.TourniquetHistory;
import uz.pdp.rest_api_jwt.entity.enums.RoleName;
import uz.pdp.rest_api_jwt.payload.LoginDto;
import uz.pdp.rest_api_jwt.payload.RegisterDto;
import uz.pdp.rest_api_jwt.repository.*;
import uz.pdp.rest_api_jwt.security.JwtProvider;

import javax.mail.internet.MimeMessage;
import javax.persistence.OneToOne;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class EmployeeService {

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    EmployeeTaskRepository employeeTaskRepository;

    @Autowired
    TourniquetHistoryRepository tourniquetHistoryRepository;

    @Autowired
    TourniquetCardRepository tourniquetCardRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JavaMailSender javaMailSender;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtProvider jwtProvider;


    public ApiResponse registerEmployee(RegisterDto registerDto) {


        // THIS EMAIL SHOULD NOT BE in DB
        boolean existsByEmail = employeeRepository.existsByEmail(registerDto.getEmail());
        if (existsByEmail) {
            return new ApiResponse("This email already exists", false);
        }

        Employee employee = new Employee();
        employee.setFirstname(registerDto.getFirstname());
        employee.setLastname(registerDto.getLastname());
        employee.setEmail(registerDto.getEmail());
        employee.setRoles(Collections.singleton(roleRepository.findByRoleName(RoleName.EMPLOYEE)));
        employee.setEmailCode(UUID.randomUUID().toString());
        employeeRepository.save(employee);
        sendEMail(employee.getEmailCode(), employee.getEmail());
        return new ApiResponse("You have successfully registered. Confirm your email to activate your account", true);
    }

    public ApiResponse employeeVerifyEmail(String emailCode, String email, String password) {

        Optional<Employee> optionalEmployee = employeeRepository.findByEmailCodeAndEmail(emailCode, email);

        if (optionalEmployee.isPresent()) {
            Employee employee = optionalEmployee.get();
            employee.setEnabled(true);
            employee.setEmailCode(null);
            employee.setPassword(passwordEncoder.encode(password));
            employeeRepository.save(employee);
            return new ApiResponse("Account verified", true);
        }
        return new ApiResponse("Account already verified", false);
    }

    public ApiResponse loginEmployee(LoginDto loginDto) {

        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginDto.getUsername(), loginDto.getPassword()));

            // Returns the User in UserDetails...
            Employee employee = (Employee) authentication.getPrincipal();
           // RETURN USERNAME TOKEN TOGETHER WITH ROLE; THE NEXT TIME USER WILL LOG IN WITH THIS TOKEN:
            String token = jwtProvider.generateToken(loginDto.getUsername(), employee.getRoles());
            return new ApiResponse("Token", true, token);

        } catch (BadCredentialsException badCredentialsException) {
            return new ApiResponse("Password or login error", false);
        }
    }

    public void sendEMail(String emailCode, String sendingEmail) {

        String link = "http://localhost:8080/api/auth/verifyEmail/employee?emailCode=" + emailCode + "&email=" + sendingEmail;
        String body = "<form action=" + link + " method=\"post\">\n" +
                "<label>Create password for your cabinet</label>" +
                "<br/><input type=\"text\" name=\"password\" placeholder=\"password\">\n" +
                "<br/> <button>Submit</button>\n" +
                "</form>";
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());
            helper.setFrom("Teat@gmail.com");
            helper.setTo(sendingEmail);
            helper.setText(body, true);
            javaMailSender.send(message);
        } catch (Exception ignored) {
        }
    }

    public ApiResponse getEmployeeByIdTimeStatus(UUID uuid, String date1, String date2) {

        Optional<Employee> optionalEmployee = employeeRepository.findById(uuid);
        if (!optionalEmployee.isPresent()) {
            return new ApiResponse("No such Employee exists", false);
        }

        Date dateA;
        Date dateB;

        DateFormat dateFormat2 = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
        try {
            dateA = dateFormat2.parse(date1);
            java.sql.Timestamp sql_Timestamp1 = new java.sql.Timestamp(dateA.getTime());

            dateB = dateFormat2.parse(date2);
            java.sql.Timestamp sql_Timestamp2 = new java.sql.Timestamp(dateB.getTime());
            List<TourniquetHistory> getInTimeBetween = tourniquetHistoryRepository.findAllByCard_EmployeeIdAndGetInTimeBetween(uuid, sql_Timestamp1, sql_Timestamp2);
            List<TourniquetHistory> getOutTimeBetween = tourniquetHistoryRepository.findAllByCard_EmployeeIdAndGetOutTimeBetween(uuid, sql_Timestamp1, sql_Timestamp2);
          //  return new ApiResponse(" Employee ", true, Collections.singletonList(getInTimeBetween));
            return new ApiResponse(" Employee ", true, Collections.singletonList(getOutTimeBetween), Collections.singletonList(getInTimeBetween));
        } catch (ParseException e) {
            return null;
        }
        // return new ApiResponse(" Employee ", true, taskList, Collections.singletonList(timeBetween));

    }


    public ApiResponse getEmployeeByIdTimeStatus1(UUID uuid, String date1, String date2) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(uuid);
        if (!optionalEmployee.isPresent()) {
            return new ApiResponse("No such Employee exists", false);
        }

        try {
            LocalDate ld1 = LocalDate.parse(date1, DateTimeFormatter.ofPattern("yyy-MM-dd"));
            LocalDate ld2 = LocalDate.parse(date2, DateTimeFormatter.ofPattern("yyy-MM-dd"));
            List<Task> taskList = employeeTaskRepository.findAllByCompletedAtBetweenAndEmployeeIdAndStatus(ld1, ld2, uuid, 3);
            return new ApiResponse(" Employee ", true, taskList);

        } catch (Exception e) {
            return new ApiResponse(" Not found ", false, null);
        }

    }

}
