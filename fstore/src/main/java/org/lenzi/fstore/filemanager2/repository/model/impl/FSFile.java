/**
 * 
 */
package org.lenzi.fstore.filemanager2.repository.model.impl;

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
@Table(name="FS_FILE")
public class FSFile extends FSPath {

	/**
	 * 
	 */
	@Transient
	private static final long serialVersionUID = 2220744795886332761L;

	/**
	 * 
	 */
	public FSFile() {

	}

}
