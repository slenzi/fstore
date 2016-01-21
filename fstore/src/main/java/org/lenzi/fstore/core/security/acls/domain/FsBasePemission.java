package org.lenzi.fstore.core.security.acls.domain;

import org.springframework.security.acls.domain.BasePermission;

/**
 * Permission class which extends from default Spring ACL BasePemission.
 * 
 * @author slenzi
 */
public class FsBasePemission extends BasePermission {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8086720220479539264L;
	

	public FsBasePemission(int mask) {
		super(mask);
	}

	public FsBasePemission(int mask, char code) {
		super(mask, code);
	}

}
