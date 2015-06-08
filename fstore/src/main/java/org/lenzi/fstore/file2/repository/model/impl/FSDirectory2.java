/**
 * 
 */
package org.lenzi.fstore.file2.repository.model.impl;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author sal
 *
 */
@Entity
@DiscriminatorValue("DIRECTORY")
@Table(name="FS_DIRECTORY_2")
public class FSDirectory2 extends FSPath {

	/**
	 * 
	 */
	@Transient
	private static final long serialVersionUID = 6270101278822512023L;

	/**
	 * 
	 */
	public FSDirectory2() {
		
	}

}
