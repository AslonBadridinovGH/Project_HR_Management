package uz.pdp.rest_api_jwt.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.pdp.rest_api_jwt.service.AuthService;
import uz.pdp.rest_api_jwt.service.DirectorService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

     @Autowired
     JwtProvider jwtProvider;

     @Autowired
     AuthService authService;


     // When logging in with a token, ENTER THE USER AFTER PASSING THE FILTER
     @Override
     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                           FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");
        if (authorization!=null&&authorization.startsWith("Bearer")){
            authorization=authorization.substring(7);
            String  emailFromToken = jwtProvider.getEmailFromToken(authorization);
            if (emailFromToken!=null){
                // FIND USERNAME FROM DB
                UserDetails userDetails = authService.loadUserByUsername(emailFromToken);

                // MUST AUTHENTICATE THE OBJECT FROM userDetails TO THE SYSTEM:
                // userDetails-username, null - password; userDetails.getAuthorities() - Roles
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails,  null,  userDetails.getAuthorities());

                // LOGIN USER TO SYSTEM
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
         }
        }
         filterChain.doFilter(request,response);
    }

}
