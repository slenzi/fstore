/**
 * 
 */
package org.lenzi.fstore.main.config;

import java.util.Arrays;
import java.util.Iterator;

import org.lenzi.fstore.core.repository.security.model.impl.FsUserRole.Role;
import org.lenzi.fstore.core.security.FsResourcePermissionEvaluator;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.main.properties.ManagedProperties;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author sal
 *
 * Setup spring security role based permissions using our custom user details service which validates users
 * against the FS_USERS table.
 * 
 * Roles are mapped to specific URL requests. See configure(HttpSecurity http) method below.
 * 
 * This class also sets up method level security using our custom permission evaluator.
 * 
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
	 * Enables method level security using Spring Security.
	 * 
	 * Allows us to annotate methods with the following annotation to control access.
	 * 
	 * @PreAuthorize("isAuthenticated() and hasPermission(#thingId, 'path.to.Thing', 'read')")
	 * 
	 * There are four possible annotations one can use, @PreAuthorize, @PreFilter, @PostAuthorize and @PostFilter.
	 * 
	 * @author sal
	 */
	@Configuration
	@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
	static class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
		
		@Autowired
		private FsResourcePermissionEvaluator fsResourcePermissionEvaluator;

		@Override
		protected MethodSecurityExpressionHandler createExpressionHandler() {
			
			DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
			expressionHandler.setPermissionEvaluator( fsResourcePermissionEvaluator );
			return expressionHandler;
			
		}
		
	}
	
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
	
	

	/**
	 * Specify which paths spring security should ignore
	 * 
	 * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.WebSecurity)
	 */
	@Override
	public void configure(WebSecurity web) throws Exception {
		
		web.ignoring()
		    .antMatchers( "/spring/file2/upload" ); // multipart file upload
		
	}

	/**
	 * Specify access rules
	 * 
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
				anyRole(
						Role.ADMINISTRATOR, 
						Role.FILE_MANAGER_USER, 
						Role.FILE_MANAGER_ADMINISTRATOR
						)
			)
			
			// cms workplace	
			.antMatchers("/cms/**").access(
				anyRole(
						Role.ADMINISTRATOR, 
						Role.CMS_WORKPLACE_USER, 
						Role.CMS_WORKPLACE_ADMINISTRATOR
						)
			)
			
			// file upload handler (used in file manager and cms workplace sections)
			//
			// IMPORTANT - Make sure to submit CSRF token as a form value when you submit the file data. If the token
			// is not part of the multipart request then Spring Security filter will reject the request.
			//
			// ALSO - Need to inject Spring MultipartFilter before Spring Security Filter.
			// see org.lenzi.fstore.main.config.SpringSecurityInitializer
			.antMatchers(HttpMethod.POST, "/spring/file2/upload").access(
				anyRole(
						Role.ADMINISTRATOR, 
						Role.FILE_MANAGER_USER, 
						Role.FILE_MANAGER_ADMINISTRATOR, 
						Role.CMS_WORKPLACE_USER, 
						Role.CMS_WORKPLACE_ADMINISTRATOR
						)
			)
			
			//.antMatchers(HttpMethod.POST, "/spring/file2/upload").permitAll()
			
			
			// cms resource dispatcher	
			.antMatchers("/spring/cms/**").access(
				anyRole(Role.USER)
			)
			
			// jax-rs service for file resource stores
			.antMatchers("/cxf/resource/store/**").access(
				anyRole(
						Role.ADMINISTRATOR, 
						Role.FILE_MANAGER_USER, 
						Role.FILE_MANAGER_ADMINISTRATOR, 
						Role.CMS_WORKPLACE_USER, 
						Role.CMS_WORKPLACE_ADMINISTRATOR
						)
			)
			
			// jax-rs service for file data
			.antMatchers("/cxf/resource/file/**").access(
				anyRole(
						Role.ADMINISTRATOR, 
						Role.FILE_MANAGER_USER, 
						Role.FILE_MANAGER_ADMINISTRATOR, 
						Role.CMS_WORKPLACE_USER, 
						Role.CMS_WORKPLACE_ADMINISTRATOR
						)	
			)
			
			// jax-rs service for directory data
			.antMatchers("/cxf/resource/directory/**").access(
				anyRole(
						Role.ADMINISTRATOR, 
						Role.FILE_MANAGER_USER, 
						Role.FILE_MANAGER_ADMINISTRATOR, 
						Role.CMS_WORKPLACE_USER, 
						Role.CMS_WORKPLACE_ADMINISTRATOR
						)		
			)
			
			// jax-rs service for cms sites
			.antMatchers("/cxf/cms/site/**").access(
				anyRole(
						Role.ADMINISTRATOR,
						Role.CMS_WORKPLACE_USER, 
						Role.CMS_WORKPLACE_ADMINISTRATOR
						)
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
	
	/**
	 * Builds a "hasRole" list, OR'ing all roles.
	 * 
	 * e.g. hasRole('admin') or hasRole('user') or hasRole('other'), etc...
	 * 
	 * @param roles
	 * @return
	 */
	private String anyRole(Role... roles){
		if(roles == null || roles.length == 0){
			return "";
		}else{
			StringBuffer buf = new StringBuffer();
			Iterator<Role> roleItr = Arrays.asList(roles).iterator();
			while(roleItr.hasNext()){
				Role role = roleItr.next();
				buf.append("hasRole('" + role.getRoleCode() + "')" + ((roleItr.hasNext()) ? " or " : ""));
			}
			return buf.toString();
		}
	}	


}
