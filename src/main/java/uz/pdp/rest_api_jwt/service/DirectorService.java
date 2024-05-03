package uz.pdp.rest_api_jwt.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.pdp.rest_api_jwt.entity.Employee;
import uz.pdp.rest_api_jwt.entity.enums.RoleName;
import uz.pdp.rest_api_jwt.payload.LoginDto;
import uz.pdp.rest_api_jwt.payload.RegisterDto;
import uz.pdp.rest_api_jwt.repository.*;
import uz.pdp.rest_api_jwt.security.JwtProvider;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
public class DirectorService {


    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JavaMailSender javaMailSender;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtProvider jwtProvider;


    public ApiResponse registerDirector(RegisterDto registerDto){

        boolean existsByEmail = employeeRepository.existsByEmail(registerDto.getEmail());
        if (existsByEmail){
            return new ApiResponse("such Email already exist",false);
        }

          Employee director=new Employee();
        director.setFirstname(registerDto.getFirstname());
        director.setLastname(registerDto.getLastname());
        director.setEmail(registerDto.getEmail());
        director.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        director.setCompany(registerDto.getCompany());
        director.setRoles(Collections.singleton(roleRepository.findByRoleName(RoleName.DIRECTOR)));
        director.setEmailCode(UUID.randomUUID().toString());
        employeeRepository.save(director);

        // WE CALL THE SEND TO EMAIL METHOD
        sendEMail(director.getEmailCode(),director.getEmail());
        return new ApiResponse("You have successfully registered, confirm your email to activate the account",true);
    }

    // We send a confirmation link to the user's email through the SimpleMailMessage class
    public void sendEMail(String emailCode, String sendingEmail){
      try {
     SimpleMailMessage mailMessage = new SimpleMailMessage();
    mailMessage.setFrom("test@gmail.com");
    mailMessage.setTo(sendingEmail);
    mailMessage.setSubject("Account verification");
    mailMessage.setText("<a href='http://localhost:8080/api/auth/verifyEmail/director?emailCode="+emailCode+"&email="+sendingEmail+"'>Verification</a>");
    javaMailSender.send(mailMessage);
    }catch (Exception ignored){

   }
}

    // THIS METHOD WORKS WHEN THE USER OPENS HIS EMAIL AND CLICKS ON THE CONFIRMATION LINK. It extracts the email and emailCode from the LINK.
    public ApiResponse directorVerifyEmail(String emailCode,String email){

        Optional<Employee> optionalUser = employeeRepository.findByEmailCodeAndEmail(emailCode,email);
        if (optionalUser.isPresent()){
               Employee director = optionalUser.get();
               director.setEnabled(true);
               director.setEmailCode(null);
               employeeRepository.save(director);
               return new ApiResponse("Account verified",true);
           }
               return new ApiResponse("Account already verified",false);
    }

    // THIS METHOD COMPARES THE USERNAME AND PASSWORD IN THE DB AND CHECKS THAT THEY ARE NOT FALSE AGAINST THE 4 BOOLEAN FIELDS IN THE USER ENTITY.
    public ApiResponse loginDirector(LoginDto loginDto) {
    try {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
        loginDto.getUsername(), loginDto.getPassword()));

        Employee director = (Employee) authentication.getPrincipal();

        // RETURN USERNAME TOKEN WITH ROLE; THE NEXT TIME THE USER LOGINS WITH THIS TOKEN:
        String token = jwtProvider.generateToken(loginDto.getUsername(),director.getRoles());
        return new ApiResponse("Token",true, token);

    }catch (BadCredentialsException  badCredentialsException){
        return new ApiResponse("Password or login error",false);
    }
  }

}


