package uz.pdp.rest_api_jwt.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.pdp.rest_api_jwt.entity.Employee;
import java.util.Optional;
import java.util.UUID;

// return User in SecurityContextHolder( SYSTEM )
// and give it to Fields createdBy and updatedBy in Product. Because they are UUID.
public class SpringSecurityAuditAwareImpl implements AuditorAware<UUID> {
    @Override
    public Optional<UUID> getCurrentAuditor() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser"))
        {
            Employee user = (Employee) authentication.getPrincipal();
            return Optional.of(user.getId());
        }
        return Optional.empty();
    }
}

