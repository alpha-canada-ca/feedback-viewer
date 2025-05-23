package ca.gc.tbs;

import ca.gc.tbs.domain.Role;
import ca.gc.tbs.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.datatables.DataTablesRepositoryFactoryBean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

//

@SpringBootApplication
@EnableMongoRepositories(repositoryFactoryBeanClass = DataTablesRepositoryFactoryBean.class)
public class PageSuccessApplication {

  public static void main(String[] args) {
    SpringApplication.run(PageSuccessApplication.class, args);
  }

  @Bean
  CommandLineRunner init(RoleRepository roleRepository) {

    return args -> {
      Role adminRole = roleRepository.findByRole("ADMIN");
      if (adminRole == null) {
        Role newAdminRole = new Role();
        newAdminRole.setRole("ADMIN");
        roleRepository.save(newAdminRole);
      }

      Role userRole = roleRepository.findByRole("USER");
      if (userRole == null) {
        Role newUserRole = new Role();
        newUserRole.setRole("USER");
        roleRepository.save(newUserRole);
      }
      Role apiRole = roleRepository.findByRole("API");
      if (apiRole == null) {
        Role newApiRole = new Role();
        newApiRole.setRole("API");
        roleRepository.save(newApiRole);
      }
    };
  }
}
