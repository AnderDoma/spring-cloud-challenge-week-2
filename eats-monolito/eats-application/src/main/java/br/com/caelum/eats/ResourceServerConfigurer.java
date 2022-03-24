package br.com.caelum.eats;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
public class ResourceServerConfigurer extends ResourceServerConfigurerAdapter {

	@Bean
	public AccessDeniedHandler accessDenied() {
		return new CustomAccessDeniedHandler();
	}
	
	@Override
	public void configure(HttpSecurity http) throws Exception {
		System.out.println("Verificando permissão");
		http.authorizeRequests().antMatchers("/admin/**", "/pedidos**").hasRole("ADM")
				.antMatchers("/pedidos**").hasRole("CLI").antMatchers(HttpMethod.GET, "/restaurantes/{id}")
				.hasAnyRole("CLI") /* em teste */
				.antMatchers(HttpMethod.POST, "/parceiros/restaurantes").hasAnyRole("ADM", "RESTAURANTE")
				.antMatchers(HttpMethod.PUT, "/parceiros/restaurantes/{id}").hasAnyRole("ADM", "RESTAURANTE")
				.and().exceptionHandling().accessDeniedHandler(accessDenied());;
	}
}
