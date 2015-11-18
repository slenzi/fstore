/**
 * 
 */
package org.lenzi.fstore.main.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author sal
 *
 * Setup spring security
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	/**
	 * 
	 */
	public SecurityConfig() {
		
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder)
	 */
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {

		auth
			.inMemoryAuthentication()
			.withUser("lenzi").password("fubar").roles("ADMIN");
		
		/*
		 * http://www.mkyong.com/spring-security/spring-security-form-login-using-database/
		 * 
	  auth.jdbcAuthentication().dataSource(dataSource)
		.usersByUsernameQuery(
			"select username,password, enabled from users where username=?")
		.authoritiesByUsernameQuery(
			"select username, role from user_roles where username=?");		 
		 */
		
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.HttpSecurity)
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		// spring security comes with a default login form. This can be overridden.
		
		// restrict access to administration section to users who have 'ADMIN' role 
		http.authorizeRequests().antMatchers("/administration/**").hasAnyRole("ADMIN","OTHER_1","OTHER_2").and().formLogin();
		
	}





}
