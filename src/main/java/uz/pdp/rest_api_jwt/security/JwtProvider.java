package uz.pdp.rest_api_jwt.security;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import uz.pdp.rest_api_jwt.entity.Role;


import java.util.Date;
import java.util.Set;

@Component
public class JwtProvider {

   private static final long expireTime = 1000 * 60 * 60 * 24;   // 1 KUN
   private static final String  secretSoz = "maxfiysozbunihechkimbilmasin";

   // GENERATE TOKEN
   public String generateToken(String username, Set<Role> roles){
      Date expireDate = new Date(System.currentTimeMillis() + expireTime);
      String token = Jwts
              .builder()
              .setSubject(username)
              .setIssuedAt(new Date())
              .setExpiration(expireDate)
              .signWith(SignatureAlgorithm.HS512,secretSoz)
              .claim("roles", roles)  // claim(name of Object, object)
              .compact();
      return  token;
   }

   // RECEIVING THE EMAIL FROM THE TOKEN WHEN THE CLIENT LOGIN WITH THE TOKEN FOR THE 2ND TIME
   public String getEmailFromToken(String token){

        try {
           String email = Jwts
                   .parser()
                   .setSigningKey(secretSoz)
                   .parseClaimsJws(token)
                   .getBody()
                   .getSubject();
           return email;
           // ANY ERROR WILL BE CACHED; For example, even though ExpiredDate divides.
        }catch (Exception e){
           return null;
        }
   }


}
