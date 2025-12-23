package ca.gc.tbs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class PageSuccessApplication {

  public static void main(String[] args) {
    SpringApplication.run(PageSuccessApplication.class, args);
  }

  // Role initialization is now handled by DataInitializer component
}
