package org.lenzi.fstore.core.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * User class which extends from the default spring security user class.
 * 
 * @author sal
 */
public class SpringSecurityUser extends User {

	private static final long serialVersionUID = -7969212657349744255L;
	
	public SpringSecurityUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
		
		super(username, password, authorities);
		
	}

	public SpringSecurityUser(String username, String password, boolean enabled, boolean accountNonExpired,
			boolean credentialsNonExpired, boolean accountNonLocked,
			Collection<? extends GrantedAuthority> authorities) {
		
		super(username, password, enabled, accountNonExpired,credentialsNonExpired, accountNonLocked, authorities);

	}

}
