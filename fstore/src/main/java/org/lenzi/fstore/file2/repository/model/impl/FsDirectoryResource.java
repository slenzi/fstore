/**
 * 
 */
package org.lenzi.fstore.file2.repository.model.impl;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author sal
 *
 */
@Entity
@DiscriminatorValue("FsDirectoryResource")
@Table(name="FS_DIRECTORY_RESOURCE")
public class FsDirectoryResource extends FsPathResource {

	/**
	 * 
	 */
	@Transient
	private static final long serialVersionUID = 6270101278822512023L;

	@Column(name = "OTHER_VALUE", nullable = false)
	private String otherValue = null;
	
	// read / write permissions.
	
	/**
	 * 
	 */
	public FsDirectoryResource() {
		
	}
	
	public FsDirectoryResource(String name, String otherValue) {
		super();
		setName(name);
		this.otherValue = otherValue;
	}	

	/**
	 * @return the otherValue
	 */
	public String getOtherValue() {
		return otherValue;
	}

	/**
	 * @param otherValue the otherValue to set
	 */
	public void setOtherValue(String otherValue) {
		this.otherValue = otherValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return FsDirectoryResource.class.getName() + " [otherValue=" + otherValue + "]";
	}

}
