package com.aluminibackend.api.seeder;

import com.aluminibackend.api.module.user.entity.User;
import com.aluminibackend.api.module.user.enums.AccountType;
import com.aluminibackend.api.module.user.enums.UserRole;
import com.aluminibackend.api.module.user.enums.UserStatus;
import com.aluminibackend.api.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
// Runs by default. Set app.seeder.enabled=false in application.properties to turn off.
@ConditionalOnProperty(
        name = "app.seeder.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class RoleUserSeeder implements ApplicationRunner {

    private static final String DEFAULT_PASSWORD = "Check@rolenow26";

    private static final Map<UserRole, String> ROLE_EMAILS = Map.of(
            UserRole.ADMIN, "admin@cognizant.com",
            UserRole.HRBP, "hrbp@cognizant.com",
            UserRole.IT_OPS, "itops@cognizant.com",
            UserRole.ALUMNI_RELATIONS, "alumnirelations@cognizant.com",
            UserRole.TALENT_ACQUISITION, "talentacquisition@cognizant.com",
            UserRole.EMPLOYEE, "employee@cognizant.com"
    );

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;



    @Override
    public void run(ApplicationArguments args) {
        ROLE_EMAILS.forEach(this::seedRoleUserIfMissing);
    }

    private void seedRoleUserIfMissing(UserRole role, String email) {

        // "First time" guard: only create if this seed user doesn't already exist.
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }

        User user = User.builder()
                .employeeId("SEED-" + role.name())
                .email(email)
                .name(displayName(role))
                .role(role)
                .phone("0000000000")
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .accountType(AccountType.ACTIVE)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);

        log.info("Seeded {} user: {}", role, email);
    }

    private String displayName(UserRole role) {
        return switch (role) {
            case ADMIN -> "Seed Admin";
            case HRBP -> "Seed HRBP";
            case IT_OPS -> "Seed IT Ops";
            case ALUMNI_RELATIONS -> "Seed Alumni Relations";
            case TALENT_ACQUISITION -> "Seed Talent Acquisition";
            case EMPLOYEE -> "Seed Employee";
        };
    }
}