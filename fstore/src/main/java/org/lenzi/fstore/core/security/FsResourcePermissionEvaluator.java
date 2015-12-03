/**
 * 
 */
package org.lenzi.fstore.core.security;

import java.io.Serializable;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Evaluates whether or not a user has access to an FsPathResource
 * 
 * http://docs.spring.io/spring-security/site/docs/4.0.3.RELEASE/reference/htmlsingle/#el-permission-evaluator
 * 
 * @author sal
 */
@Component
public class FsResourcePermissionEvaluator implements PermissionEvaluator {

	@InjectLogger
	private Logger logger;
	
	/**
	 * 
	 */
	public FsResourcePermissionEvaluator() {
	
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.access.PermissionEvaluator#hasPermission(org.springframework.security.core.Authentication, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
	
		// Used in situations where the domain object, to which access is being controlled, is already loaded. 
		// Then expression will return true if the current user has the given permission for that object.
		
		System.out.println("HasPermission(Authentication authentication, Object targetDomainObject, Object permission) called.");
		
		debugUsername(authentication);
		
		return true;
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.access.PermissionEvaluator#hasPermission(org.springframework.security.core.Authentication, java.io.Serializable, java.lang.String, java.lang.Object)
	 */
	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
	
		// Used in cases where the object is not loaded, but its identifier is known. An abstract "type" specifier for the 
		// domain object is also required, allowing the correct ACL permissions to be loaded. This has traditionally 
		// been the Java class of the object, but does not have to be as long as it is consistent with how the 
		// permissions are loaded.
		
		System.out.println("HasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) called.");
		
		debugUsername(authentication);
		
		return true;
	}
	
	private void debugUsername(Authentication auth){
		if(auth != null){
			Object principal = auth.getPrincipal();
			String username = "unknown";
			if (principal instanceof UserDetails) {
				username = ((UserDetails)principal).getUsername();
			} else {
				username = principal.toString();
			}
			if(logger != null){
				logger.debug("Username => " + username);
			}else{
				System.out.println("Username: " + username);
			}
		}
	}

}
