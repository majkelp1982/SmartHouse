package pl.pomazanka.SmartHouse.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import pl.pomazanka.SmartHouse.backend.communication.MongoDBController;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired MongoDBController mongoDBController;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Override
  @Bean
  public UserDetailsService userDetailsService() {
    final UserDetails user = mongoDBController.getUser();

    return new InMemoryUserDetailsManager(user);
  }

  @Override
  protected void configure(final HttpSecurity http) throws Exception {
    http.csrf().disable();
    http.authorizeRequests().anyRequest().permitAll().and().formLogin().permitAll();
  }
}
