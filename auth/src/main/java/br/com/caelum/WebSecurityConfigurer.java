package br.com.caelum;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter{
	
	@Override @Bean
	protected AuthenticationManager authenticationManager() throws Exception {
		System.out.println("authenticationManager");
		return super.authenticationManager();
	}
	
	@Override @Bean
	protected UserDetailsService userDetailsService() {
		return super.userDetailsService();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}


	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication().passwordEncoder(passwordEncoder())
		.withUser("anderson")
		.password(passwordEncoder().encode("anderpwd"))
		.roles("ABC").and().withUser("admin")
		.password("adminpwd").roles("ADM").and().withUser("cliente")
		.password("clipwd").roles("CLI");
	}
	
	@Override
	public void configure(HttpSecurity http) throws Exception {
		System.out.println("Verificando permissão");
		http.authorizeRequests().antMatchers("/admin/**", "/pedidos**").hasRole("ADM")
				.antMatchers("/pedidos**").hasRole("CLI").antMatchers(HttpMethod.GET, "/restaurantes/{id}")
				.hasAnyRole("CLI") /* em teste */
				.antMatchers(HttpMethod.POST, "/parceiros/restaurantes").hasAnyRole("ADM", "RESTAURANTE")
				.antMatchers(HttpMethod.PUT, "/parceiros/restaurantes/{id}").hasAnyRole("ADM", "RESTAURANTE")
				.and().authorizeRequests();
	}

}
