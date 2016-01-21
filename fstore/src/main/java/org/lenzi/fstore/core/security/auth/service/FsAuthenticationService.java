package org.lenzi.fstore.core.security.auth.service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.lenzi.fstore.core.repository.security.model.impl.FsUser;
import org.lenzi.fstore.core.security.FsSecureUser;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * For authentication related operations.
 * 
 * @author sal
 */
@Service("fsAuthenticationManager")
public class FsAuthenticationService {

	@InjectLogger
	private Logger logger;	
	
	/**
	 * Get access to the Spring Authentication Manager which users our custom UserDetailsService (FsUserDetailsService)
	 * to authenticate users against our local database.
	 * 
	 * See the SecurityConfig class for more details.
	 */
	@Autowired
	@Qualifier("fsLocalAuthenticationManager")
	private AuthenticationManager fsAuthenticationManager;
	
	/**
	 * Qualifier specifies instance of org.lenzi.fstore.core.security.service.FsUserDetailsService
	 */
	@Autowired
	@Qualifier("fsUserDetailsService")	
	private UserDetailsService userDetailsService;
	
	public FsAuthenticationService() {
		
	}
	
	/**
	 * Load user authentication object into security context for current thread-local.
	 * 
	 * @param username - username of user
	 * @throws ServiceException
	 */
	public void configureAuthentication(final String username)  throws ServiceException {
		
		UserDetails user = userDetailsService.loadUserByUsername(username);
		
		Authentication authentication =  new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
	}
	
	/**
	 * Clear authentication object from security context for current thread-local.
	 */
	public void clearAuthentication(){
		
		SecurityContextHolder.getContext().setAuthentication(null);
		
	}
	
	/**
	 * Get username of principal currently interacting with the application (the authenticated spring user.)
	 * 
	 * @return
	 * @throws ServiceException
	 */
	public String getUsername() {
		
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		String username = "unknown";
		
		if (principal instanceof UserDetails) {
			username = ((UserDetails)principal).getUsername();
		} else {
			username = principal.toString();
		}		
		
		return username;
	}
	
	/**
	 * Get authorities (roles) of principal currently interacting with the application (the authenticated spring user.)
	 * 
	 * @return A list of role codes (e.g. 'ROLE_ADMINISTRATOR', 'ROLE_OTHER', etc..)
	 * @throws ServiceException
	 */
	public List<String> getAuthorities() {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		Collection<SimpleGrantedAuthority> authorities = (Collection<SimpleGrantedAuthority>)auth.getAuthorities();
		
		List<String> roles = authorities.stream().map(
					SimpleGrantedAuthority::getAuthority).collect(Collectors.toList());
		
		return roles;
	}
	
	/**
	 * Get principal currently interacting with the application (the authenticated spring user.)
	 * 
	 * @return A FsSecureUser object which extends from the default org.springframework.security.core.userdetails.User class.
	 * 	Otherwise null.
	 */
	public FsSecureUser getLoggedInUser() {
		
		SecurityContext context = SecurityContextHolder.getContext();
		if(context == null){
			logError("Security context is null.",null);
			return null;
		}
		Authentication auth = context.getAuthentication();
		if(auth == null){
			logError("Authentication is null",null);
			return null;
		}
		Object principal = auth.getPrincipal();
		if(principal == null){
			logError("Object principal is null",null);
			return null;
		}
		
		FsSecureUser user = null;
		
		if (principal instanceof FsSecureUser) {
			user = (FsSecureUser)principal;
		}	
		
		return user;	
	}
	
	/**
	 * Fetch the current principal interacting with the application, and log the user details
	 */
	public void logDebugUserDetails(){
		logDebugUserDetails(getLoggedInUser());
	}
	
	/**
	 * Log the details of the user
	 * 
	 * @param user
	 */
	public void logDebugUserDetails(FsSecureUser user){

		boolean havePricipalUser = (user != null) ? true : false;
		
		
		String username = null;
		Collection<GrantedAuthority> authorities = null;
		if(havePricipalUser){
			
			username = user.getUsername();
			authorities = user.getAuthorities();
			
			boolean haveFsUser = (havePricipalUser && user.getFsUser() != null) ? true : false;
			
			logger.debug("Logged in user details: ");
			logger.debug("Have principal user (spring security) => " + havePricipalUser);
			logger.debug("Have FsUser => " + haveFsUser);
			
			if(authorities != null){
				authorities.forEach((authority) -> {
					logger.debug("Granted Authority => " + authority.toString());
				});
			}		
			
			logger.debug("Username => " + username);
			
			if(haveFsUser){
				FsUser fsUser = user.getFsUser();
				logger.debug("User ID => " + fsUser.getUserId());
				logger.debug("First name => " + fsUser.getFirstName());
				logger.debug("Last name => " + fsUser.getLastName());
				logger.debug("Primary Email => " + fsUser.getPrimaryEmail());
				logger.debug("Role count => " + fsUser.roleCount());
				logger.debug("Group count => " + fsUser.groupCount());
			}		
			
		}else{
			logger.warn("Cannot log user details. FsSecureUser object is null.");
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
