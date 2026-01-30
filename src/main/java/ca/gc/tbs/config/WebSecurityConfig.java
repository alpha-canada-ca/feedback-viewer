package ca.gc.tbs.config; // package ca.gc.tbs.config;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.thymeleaf.extras.springsecurity4.dialect.SpringSecurityDialect;

import ca.gc.tbs.security.JWTFilter;
import ca.gc.tbs.service.UserService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;

  @Autowired CustomizeAuthenticationSuccessHandler customizeAuthenticationSuccessHandler;

  @Autowired private UserService myUserDetailsService;

  @Autowired private JWTFilter jwtFilter;

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    UserService userDetailsService = myUserDetailsService;
    auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf()
        .disable()
        .authorizeRequests()
        .antMatchers("/createApiUser")
        .hasAuthority("ADMIN")
        .antMatchers("/authenticate")
        .permitAll()
        .antMatchers("/api/user/**")
        .hasRole("USER")
        .antMatchers("/", "/checkExists", "/error", "/enableAdmin", "/login", "/signup", "/success")
        .permitAll()
        .antMatchers("/u/**")
        .hasAnyAuthority("ADMIN")
        .antMatchers("/keywords/**")
        .hasAnyAuthority("ADMIN")
        .antMatchers("/python/**", "/reports/**", "/dashboard/**")
        .hasAnyAuthority("USER", "ADMIN")
        .anyRequest()
        .authenticated()
        .and()
        .formLogin()
        .loginPage("/login")
        .permitAll()
        .successHandler(customizeAuthenticationSuccessHandler)
        .failureUrl("/login?error=true")
        .usernameParameter("email")
        .passwordParameter("password")
        .and()
        .logout()
        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
        .logoutSuccessUrl("/login?logout=true")
        .and()
        .exceptionHandling()
        .authenticationEntryPoint(
            (request, response, authException) -> {
              if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
              } else {
                response.sendRedirect("/login");
              }
            });

    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring()
        .antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**", "/**/*.js");
  }

  @Bean
  public SpringSecurityDialect springSecurityDialect() {
    return new SpringSecurityDialect();
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }
}
