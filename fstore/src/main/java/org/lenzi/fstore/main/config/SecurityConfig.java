/**
 * 
 */
package org.lenzi.fstore.main.config;

import org.lenzi.fstore.core.repository.security.model.impl.FsUserRole.Role;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author sal
 *
 * Setup spring security
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@InjectLogger
	private Logger logger;	
	
	/**
	 * Qualifier specifies instance of org.lenzi.fstore.core.security.service.FsUserDetailsService
	 */
	@Autowired
	@Qualifier("FsUserDetailsService")	
	private UserDetailsService userDetailsService;
	
	/**
	 * 
	 */
	public SecurityConfig() {
		
	}
	
	// name of method not important
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

		//logger.debug(SecurityConfig.class.getName() + ".configureGlobal(...) called ");
		
		auth.userDetailsService(userDetailsService);
		
		// uses password encryption/encoding
		//auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
		
		/*
		auth
			.inMemoryAuthentication()
			.withUser("lenzi").password("fubar").roles("ADMIN");
		*/
		
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
		
		//
		// Restrict access to administration section to users who have 'ADMINISTRATOR' role.
		//
		//http.authorizeRequests().antMatchers("/administration/**").hasRole( Role.ADMINISTRATOR.getRoleCode() ).and().formLogin();
		
		http.authorizeRequests()
			.antMatchers("/administration/**").access("hasRole('" + Role.ADMINISTRATOR.getRoleCode() + "')").and().formLogin();
		
		logger.debug("/administration/** is mapped to role '" + Role.ADMINISTRATOR.getRoleCode() + "'");
		
	}

	/**
	 * For encrypting/encoding passwords
	 * 
	 * @return
	 */
	/*
	@Bean
	public PasswordEncoder passwordEncoder(){
		PasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder;
	}
	*/


}
