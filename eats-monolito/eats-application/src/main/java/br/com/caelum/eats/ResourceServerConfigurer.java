package br.com.caelum.eats;


import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

@Configuration
public class ResourceServerConfigurer extends ResourceServerConfigurerAdapter {

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
		.antMatchers("/admin/**", "/pedidos**").hasRole("ADMIN")
		.antMatchers("/pedidos**").hasRole("CLI")
	    .antMatchers(HttpMethod.GET, "/restaurantes/{id}")
	    .hasAnyRole("CLI")
	    .antMatchers(HttpMethod.POST, "/parceiros/restaurantes")
	    .hasAnyRole("ADMIN", "RESTAURANTE")
	    .antMatchers(HttpMethod.PUT, "/parceiros/restaurantes/{id}")
	    .hasAnyRole("ADMIN", "RESTAURANTE");
	}
}
