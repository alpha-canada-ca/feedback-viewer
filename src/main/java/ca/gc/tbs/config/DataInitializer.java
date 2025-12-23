package ca.gc.tbs.config;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ca.gc.tbs.domain.Role;
import ca.gc.tbs.domain.User;
import ca.gc.tbs.repository.RoleRepository;
import ca.gc.tbs.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    initializeRoles();
    initializeDevUser();
  }

  private void initializeRoles() {
    // Create default roles if they don't exist
    createRoleIfNotExists("USER");
    createRoleIfNotExists("ADMIN");
    createRoleIfNotExists("API");
    LOGGER.info("Default roles initialized successfully.");
  }

  private void initializeDevUser() {
    // Create a default admin user for development testing
    String devEmail = "admin@test.com";
    User existingUser = userRepository.findByEmail(devEmail);
    
    if (existingUser == null) {
      User adminUser = new User();
      adminUser.setEmail(devEmail);
      adminUser.setPassword(passwordEncoder.encode("admin123"));
      adminUser.setEnabled(true);
      adminUser.setDateCreated(LocalDateTime.now().toString());
      adminUser.setInstitution("Test Institution");
      
      // Add both USER and ADMIN roles
      Set<Role> roles = new HashSet<>();
      Role userRole = roleRepository.findByRole("USER");
      Role adminRole = roleRepository.findByRole("ADMIN");
      if (userRole != null) roles.add(userRole);
      if (adminRole != null) roles.add(adminRole);
      adminUser.setRoles(roles);
      
      userRepository.save(adminUser);
      LOGGER.info("Created default admin user: {} (password: admin123)", devEmail);
    } else {
      LOGGER.debug("Dev admin user already exists: {}", devEmail);
    }
  }

  private void createRoleIfNotExists(String roleName) {
    Role existingRole = roleRepository.findByRole(roleName);
    if (existingRole == null) {
      Role role = new Role(roleName);
      roleRepository.save(role);
      LOGGER.info("Created role: {}", roleName);
    } else {
      LOGGER.debug("Role already exists: {}", roleName);
    }
  }
}
