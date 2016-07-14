package org.lenzi.fstore.test.core.setup.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.lenzi.fstore.core.repository.security.model.impl.FsUserRole.Role;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.main.config.SecurityConfig;
import org.lenzi.fstore.main.properties.ManagedProperties;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleHierarchyVoter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;

/**
 * Spring security setup used by postgres unit tests
 * 
 * @author slenzi
 */
@Configuration
@EnableWebSecurity
public class TestConfigSpringSecurity extends WebSecurityConfigurerAdapter {

	@InjectLogger
	private Logger logger;
	
    @Autowired
    private ManagedProperties appProps;
	
	/**
	 * Qualifier specifies instance of org.lenzi.fstore.core.security.service.FsUserDetailsService
	 */
	@Autowired
	@Qualifier("fsUserDetailsService")	
	private UserDetailsService userDetailsService;
	
	/**
	 * See getAccessDecisionManager(...) method in this class
	 */
	@Autowired
	private AffirmativeBased accessDecisionManager;
	
	/**
	 * See roleHierarchy(...) method in this class
	 */
	@Autowired
	private RoleHierarchyImpl roleHierarchy;
	
	public TestConfigSpringSecurity() {
		
	}
	
	// name of method not important
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

		logInfo(SecurityConfig.class.getName() + ".configureGlobal(...) called ");
		
		auth.userDetailsService(userDetailsService);
		
	}
	
	/**
	 * Exposes the spring authentication manager that was created via the configureGlobal(AuthenticationManagerBuilder auth)
	 * method above.
	 */
	@Bean(name="fsLocalAuthenticationManager")
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	/**
	 * Specify which paths spring security should ignore
	 * 
	 * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.WebSecurity)
	 */
	@Override
	public void configure(WebSecurity web) throws Exception {
		
		logInfo(SecurityConfig.class.getName() + ".configure(WebSecurity...) called ");
		
		// no need to ignore, we are now passing the CSRF token for each upload.
		//web.ignoring().antMatchers( "/spring/file2/upload" ); // multipart file upload
		
	}

	/**
	 * Specify access rules
	 * 
	 * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.HttpSecurity)
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		logInfo(SecurityConfig.class.getName() + ".configure(HttpSecurity...) called ");
		logInfo("Have AffirmativeBased (access decision manager)  => " + ((accessDecisionManager != null) ? "true" : "false"));
		
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
		
		// works
		http.authorizeRequests()
		
			//.expressionHandler(webExpressionHandler())
			
			// our decision manager understands our hierarchical roles (using a RoleHierarchyVoter)
			.accessDecisionManager(accessDecisionManager)
		
			.antMatchers("/").permitAll()
			
			// must be a user in order to log in
			.antMatchers("/spring/core/home").access("hasRole('" + Role.USER.getRoleCode() + "')")
		
			// administration section
			.antMatchers("/administration/**").access("hasRole('" + Role.ADMINISTRATOR.getRoleCode() + "')")
			
			// file manager	
			.antMatchers("/file/**").access(
				anyRole(
						Role.FILE_MANAGER_USER
						)
			)
			
			// cms workplace	
			.antMatchers("/cms/**").access(
				anyRole(
						Role.CMS_WORKPLACE_USER
						)
			)
			
			// File upload handler (used in File Manager and CMS Workplace sections)
			//
			// IMPORTANT - Make sure to submit CSRF token as a form value when you submit the file data. If the token
			// is not part of the multipart request then Spring Security filter will reject the request.
			//
			// ALSO - Need to inject Spring MultipartFilter before Spring Security Filter.
			// see org.lenzi.fstore.main.config.SpringSecurityInitializer
			.antMatchers(HttpMethod.POST, "/spring/file2/upload").access(
				
					// allow uploads from those with the 'File Manager User' role or 'CMS Workplace User' role (including all parent roles)
					// OR any upload from loopback/localhost address 127.0.0.1
					anyRole(Role.FILE_MANAGER_USER, Role.CMS_WORKPLACE_USER) + " or " + "hasIpAddress('127.0.0.1/32')"
			)
			
			//.antMatchers(HttpMethod.POST, "/spring/file2/upload").permitAll()
			
			// for now we don't identify users that are not logged in (guests, or basic users) so we can't
			// apply security to cms sites. security should be built into the site itself (group roles applied to resources.)
			//
			// cms resource dispatcher	
			//.antMatchers("/spring/cms/**").access(
			//	anyRole(Role.USER)
			//)
			
			// jax-rs service for file resource stores
			.antMatchers("/cxf/resource/store/**").access(
				anyRole(
						Role.FILE_MANAGER_USER, Role.CMS_WORKPLACE_USER
						)
			)
			
			// jax-rs service for file data
			.antMatchers("/cxf/resource/file/**").access(
				anyRole(
						Role.FILE_MANAGER_USER, Role.CMS_WORKPLACE_USER
						)	
			)
			
			// jax-rs service for directory data
			.antMatchers("/cxf/resource/directory/**").access(
				anyRole(
						Role.FILE_MANAGER_USER, Role.CMS_WORKPLACE_USER
						)		
			)
			
			// jax-rs service for cms sites
			.antMatchers("/cxf/cms/site/**").access(
				anyRole(
						Role.CMS_WORKPLACE_USER, Role.CMS_WORKPLACE_ADMINISTRATOR
						)
			)			

			.and().formLogin().loginPage("/spring/core/login").permitAll();
		
	}
	
	/**
	 * Define role hierarchies. Higher level roles gain all permissions of the roles under them.
	 * 
	 * Roles, in order of permission level, from most access the least access.
	 * 
	 * Role.ADMINISTRATOR
	 * Role.FILE_MANAGER_ADMINISTRATOR & Role.CMS_WORKPLACE_ADMINISTRATOR
	 * Role.FILE_MANAGER_USER & Role.CMS_WORKPLACE_USER
	 * Role.USER
	 * Role.GUEST
	 * 
	 * @return
	 */
	@Bean
	public RoleHierarchyImpl roleHierarchy() {
	    
		logInfo(SecurityConfig.class.getName() + ".roleHierarchy(...) called ");
		
		RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
		
	    roleHierarchy.setHierarchy(
	    		
	    		// admins are also file manager & cms workplace admins
	    		Role.ADMINISTRATOR.getRoleCode() + " > " + Role.FILE_MANAGER_ADMINISTRATOR.getRoleCode() + " " +
	    		Role.ADMINISTRATOR.getRoleCode() + " > " + Role.CMS_WORKPLACE_ADMINISTRATOR.getRoleCode() + " " +
	    		
	    		// file manager admins are also file manager users
	    		Role.FILE_MANAGER_ADMINISTRATOR.getRoleCode() + " > " + Role.FILE_MANAGER_USER.getRoleCode() + " " +
	    		
	    		// cms workplace admins are also cms workplace users
	    		Role.CMS_WORKPLACE_ADMINISTRATOR.getRoleCode() + " > " + Role.CMS_WORKPLACE_USER.getRoleCode() + " " +
	    		
	    		// file manager users and cms workplace users are also 'users'
	    		Role.FILE_MANAGER_USER.getRoleCode() + " > " + Role.USER.getRoleCode() + " " +
	    		Role.CMS_WORKPLACE_USER.getRoleCode() + " > " + Role.USER.getRoleCode() + " " +
	    		
	    		// all users have guest privileges.
	    		Role.USER.getRoleCode() + " > " + Role.GUEST.getRoleCode()
	    		
		    );		

	    return roleHierarchy;
	}
	
	/**
	 * Access decision manager uses our role hierarchy implementation above.
	 * 
	 * See roleHierarchy() method in this class.
	 * 
	 * @return
	 */
	@Bean
	public AffirmativeBased getAccessDecisionManager(RoleHierarchy roleHierarchy) {

		logInfo(SecurityConfig.class.getName() + ".getAccessDecisionManager(...) called ");
		logInfo("Have RoleHierarchy => " + ((roleHierarchy != null) ? "true" : "false"));
		
		WebExpressionVoter webExpressionVoter = new WebExpressionVoter();
		DefaultWebSecurityExpressionHandler expressionHandler = new DefaultWebSecurityExpressionHandler();
		
		expressionHandler.setRoleHierarchy(roleHierarchy);
		webExpressionVoter.setExpressionHandler(expressionHandler);

		List<AccessDecisionVoter<? extends Object>> voters = new ArrayList<>();

		voters.add(webExpressionVoter);
		return new AffirmativeBased(voters);

	}	
	
	/**
	 * Configure a role hierarchy voter to use our role hierarchy implementation
	 * 
	 * See roleHierarchy() method in this class.
	 * 
	 * @param roleHierarchy
	 * @return
	 */
	@Bean
	public RoleHierarchyVoter roleHierarchyVoter(RoleHierarchy roleHierarchy) {
		
		logInfo(SecurityConfig.class.getName() + ".roleHierarchyVoter(RoleHierarchy...) called ");
		logInfo("Have RoleHierarchy => " + ((roleHierarchy != null) ? "true" : "false"));
		
		return new RoleHierarchyVoter(roleHierarchy);
	}	
	
	/*
	private SecurityExpressionHandler<FilterInvocation> webExpressionHandler() {
	    DefaultWebSecurityExpressionHandler defaultWebSecurityExpressionHandler = new DefaultWebSecurityExpressionHandler();
	    defaultWebSecurityExpressionHandler.setRoleHierarchy(roleHierarchy());
	    return defaultWebSecurityExpressionHandler;
	}
	*/	

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
	
	private void logInfo(String message){
		if(logger != null){
			logger.info(message);
		}else{
			System.out.println("> " + message);
		}
	}
	
	private void logError(String message, Throwable t){
		if(logger != null){
			logger.error(message, t);
		}else{
			System.err.println("> " + message + ((t != null) ? " " + t.getMessage() : ""));
		}
	}

}
