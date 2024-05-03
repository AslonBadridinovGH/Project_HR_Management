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
import org.springframework.web.servlet.view.RedirectView;
import uz.pdp.rest_api_jwt.entity.Employee;
import uz.pdp.rest_api_jwt.entity.enums.RoleName;
import uz.pdp.rest_api_jwt.payload.LoginDto;
import uz.pdp.rest_api_jwt.payload.RegisterDto;
import uz.pdp.rest_api_jwt.repository.*;
import uz.pdp.rest_api_jwt.security.JwtProvider;

import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
public class ManagerService {

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JavaMailSender javaMailSender;

    // Autowire the Method that returns the AuthenticationManager in the SecurityConfig Class.
    // CLASS THAT AUTOMATICALLY AUTHENTICATE USER AND PASSWORD
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtProvider jwtProvider;


    public ApiResponse registerManager(RegisterDto registerDto){

        boolean existsByEmail = employeeRepository.existsByEmail(registerDto.getEmail());
        if (existsByEmail){
            return new ApiResponse("This email already exists",false);
        }

          Employee manager=new Employee();
        manager.setFirstname(registerDto.getFirstname());
        manager.setLastname(registerDto.getLastname());
        manager.setEmail(registerDto.getEmail());
        manager.setRoles(Collections.singleton(roleRepository.findByRoleName(RoleName.MANAGER)));
        manager.setEmailCode(UUID.randomUUID().toString());
        employeeRepository.save(manager);

        sendEMail(manager.getEmailCode(), manager.getEmail());
        return new ApiResponse("You have successfully registered. Confirm your email to activate your account",true);
    }

    public void sendEMail(String emailCode, String sendingEmail){

        String link = "http://localhost:8080/api/auth/verifyEmail/manager?emailCode=" + emailCode + "&email=" + sendingEmail;
        String body = "<form action=" + link + " method=\"post\">\n" +
                "<label>Create password for your cabinet</label>" +
                "<br/><input type=\"text\" name=\"password\" placeholder=\"password\">\n" +
                "<br/> <button>Submit</button>\n" +
                "</form>";
        try {
             MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper=new MimeMessageHelper(message,MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());
            helper.setFrom("Teat@gmail.com");
            helper.setTo(sendingEmail);
            helper.setText(body,true);
            javaMailSender.send(message);
        }catch (Exception ignore){

        }
    }

    public ApiResponse managerVerifyEmail(String emailCode, String email, String password){

        Optional<Employee> optionalManager = employeeRepository.findByEmailCodeAndEmail(emailCode, email);
        if (optionalManager.isPresent()){
             Employee manager = optionalManager.get();
            manager.setEnabled(true);
            manager.setEmailCode(null);
            manager.setPassword(passwordEncoder.encode(password));
            employeeRepository.save(manager);
            return new ApiResponse("Account confirmed",true);
        }
        return new ApiResponse("Account already confirmed",false);
    }

    public ApiResponse loginManager(LoginDto loginDto) {

        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginDto.getUsername(), loginDto.getPassword()));

            // Returns the User in UserDetails...
            Employee manager = (Employee) authentication.getPrincipal();
            // RETURN USERNAME TOKEN TOGETHER WITH ROLE; THE NEXT TIME USER WILL LOGIN WITH THIS TOKEN:
            String token = jwtProvider.generateToken(loginDto.getUsername(), manager.getRoles());
            return new ApiResponse("Token",true,token);

        }catch (BadCredentialsException  badCredentialsException){
            return new ApiResponse("Password  login error",false);
        }}



   /*
            RedirectView redirectView=new RedirectView("");
            String text="http://localhost:63342/REST_API_JWT/Email.html?_ijt=gr1pki450mh63m4scvujclhsur&_ij_reload=RELOAD_ON_SAVE";
    */

}






