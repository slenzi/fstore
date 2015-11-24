/**
 * 
 */
package org.lenzi.fstore.main.config;

import org.lenzi.fstore.core.repository.security.model.impl.FsUserRole.Role;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.main.properties.ManagedProperties;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

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
	
    @Autowired
    private ManagedProperties appProps;
	
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
		
		String appContext = "/fstore";
		
		if(appProps != null){
			appContext = appProps.getProperty("application.context");
		}else{
			System.err.println("Error, no autowired properties in " + SecurityConfig.class.getName());
		}
		
		if(logger != null){
			logger.debug("appContext => " + appContext);
		}else{
			System.err.println("Error, no autowired logger in " + SecurityConfig.class.getName());
		}
		
		// spring security comes with a default login form. This can be overridden.
		
		//
		// Restrict access to administration section to users who have 'ADMINISTRATOR' role.
		//
		//http.authorizeRequests().antMatchers("/administration/**").hasRole( Role.ADMINISTRATOR.getRoleCode() ).and().formLogin();
		
		// works
		http.authorizeRequests()
			
			.antMatchers("/").permitAll()
		
			.antMatchers("/administration/**").access("hasRole('" + Role.ADMINISTRATOR.getRoleCode() + "')")
			
			// file manager	
			.antMatchers("/file/**").access(
				"hasRole('" + Role.ADMINISTRATOR.getRoleCode() + "') or " +
				"hasRole('" + Role.FILE_MANAGER_USER.getRoleCode() + "') or " +
				"hasRole('" + Role.FILE_MANAGER_ADMINISTRATOR.getRoleCode() + "')"
			)
			
			// cms workplace	
			.antMatchers("/cms/**").access(
				"hasRole('" + Role.ADMINISTRATOR.getRoleCode() + "') or " +
				"hasRole('" + Role.CMS_WORKPLACE_USER.getRoleCode() + "') or " +
				"hasRole('" + Role.CMS_WORKPLACE_ADMINISTRATOR.getRoleCode() + "')"
			)	

			.and().formLogin();
		
		/*
		http.authorizeRequests()
			
			// start with open access
			//.antMatchers("/").permitAll()
			
			
			// administration section
			.antMatchers(appContext + "/administration/**").hasRole(
				Role.ADMINISTRATOR.getRoleCode()
			
			)
			
			.and().formLogin();
				

			// file manager		
			).antMatchers(appContext + "/file/**").hasAnyRole(
				Role.ADMINISTRATOR.getRoleCode(),
				Role.FILE_MANAGER_USER.getRoleCode(),
				Role.FILE_MANAGER_ADMINISTRATOR.getRoleCode()
			
			// cms workplace
			).antMatchers(appContext + "/cms/**").hasAnyRole(
				Role.ADMINISTRATOR.getRoleCode(),
				Role.CMS_WORKPLACE_USER.getRoleCode(),
				Role.CMS_WORKPLACE_ADMINISTRATOR.getRoleCode()
				
			)
			

			// file upload handler (used in file manager and cms workplace sections)
			).antMatchers(appContext + "/spring/file2/**").hasAnyRole(
				Role.ADMINISTRATOR.getRoleCode(),
				Role.FILE_MANAGER_USER.getRoleCode(),
				Role.FILE_MANAGER_ADMINISTRATOR.getRoleCode(),					
				Role.CMS_WORKPLACE_USER.getRoleCode(),
				Role.CMS_WORKPLACE_ADMINISTRATOR.getRoleCode()
			
			// cms resource dispatcher
			).antMatchers(appContext + "/spring/cms/**").hasAnyRole(
				Role.USER.getRoleCode()			
			
			// jax-rs service for file resource stores
			).antMatchers(appContext + "/cxf/resource/store/**").hasAnyRole(
				Role.ADMINISTRATOR.getRoleCode(),
				Role.FILE_MANAGER_USER.getRoleCode(),
				Role.FILE_MANAGER_ADMINISTRATOR.getRoleCode(),					
				Role.CMS_WORKPLACE_USER.getRoleCode(),
				Role.CMS_WORKPLACE_ADMINISTRATOR.getRoleCode()		
			
			// jax-rs service for file data
			).antMatchers(appContext + "/cxf/resource/file/**").hasAnyRole(
				Role.ADMINISTRATOR.getRoleCode(),
				Role.FILE_MANAGER_USER.getRoleCode(),
				Role.FILE_MANAGER_ADMINISTRATOR.getRoleCode(),					
				Role.CMS_WORKPLACE_USER.getRoleCode(),
				Role.CMS_WORKPLACE_ADMINISTRATOR.getRoleCode()		
			
			// jax-rs service for directory data
			).antMatchers(appContext + "/cxf/resource/directory/**").hasAnyRole(
				Role.ADMINISTRATOR.getRoleCode(),
				Role.FILE_MANAGER_USER.getRoleCode(),
				Role.FILE_MANAGER_ADMINISTRATOR.getRoleCode(),					
				Role.CMS_WORKPLACE_USER.getRoleCode(),
				Role.CMS_WORKPLACE_ADMINISTRATOR.getRoleCode()		
			
			// jax-rs service for cms sites
			).antMatchers(appContext + "/cxf/cms/site/**").hasAnyRole(
				Role.ADMINISTRATOR.getRoleCode(),				
				Role.CMS_WORKPLACE_USER.getRoleCode(),
				Role.CMS_WORKPLACE_ADMINISTRATOR.getRoleCode()		
			
			)
			*/
			
			
		
		
		
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
