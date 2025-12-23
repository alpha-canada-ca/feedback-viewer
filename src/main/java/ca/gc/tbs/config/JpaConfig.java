package ca.gc.tbs.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.datatables.repository.DataTablesRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "ca.gc.tbs.repository",
    repositoryFactoryBeanClass = DataTablesRepositoryFactoryBean.class
)
public class JpaConfig {
  // JPA DataTables configuration
  // The DataTablesRepositoryFactoryBean enables DataTables integration with JPA repositories
}
