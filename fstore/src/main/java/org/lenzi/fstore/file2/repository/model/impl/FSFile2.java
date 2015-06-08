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
@DiscriminatorValue("FILE")
@Table(name="FS_FILE_2")
public class FSFile2 extends FSPath {

	/**
	 * 
	 */
	@Transient
	private static final long serialVersionUID = 2220744795886332761L;

	/**
	 * 
	 */
	public FSFile2() {

	}

}
