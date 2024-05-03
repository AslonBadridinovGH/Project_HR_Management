package uz.pdp.rest_api_jwt.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.rest_api_jwt.payload.LoginDto;
import uz.pdp.rest_api_jwt.payload.RegisterDto;
import uz.pdp.rest_api_jwt.service.ApiResponse;
import uz.pdp.rest_api_jwt.service.ManagerService;

@RestController
@RequestMapping("/api/auth")
public class ManagerController {

    @Autowired
    ManagerService managerService;

    @PreAuthorize(value ="hasRole('DIRECTOR')")
    @PostMapping(value = "/register/manager")
    public HttpEntity<?> registerManager(@RequestBody RegisterDto registerDto) {
        ApiResponse apiResponse = managerService.registerManager(registerDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 201 : 409).body(apiResponse);
    }

    @PostMapping(value = "/login/manager")
    public HttpEntity<?> loginManager(@RequestBody LoginDto loginDto) {
        ApiResponse apiResponse = managerService.loginManager(loginDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 401).body(apiResponse);
    }

    // VERIFY IN EMAIL THIS METHOD WORKS WHEN LINK IS CLICKED AND EXTRACTS EMAIL AND EMAILCODE FROM THE LINK.
    @PostMapping(value = "/verifyEmail/manager")
    public HttpEntity<?> verifyEmail(@RequestParam String emailCode, @RequestParam String email,@RequestParam String password) {
        ApiResponse apiResponse = managerService.managerVerifyEmail(emailCode, email, password);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }


}






