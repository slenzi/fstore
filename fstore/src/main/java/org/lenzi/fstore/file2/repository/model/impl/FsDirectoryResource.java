/**
 * 
 */
package org.lenzi.fstore.file2.repository.model.impl;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

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
	
	// path relative to store root dir path
	@Column(name = "RELATIVE_DIR_PATH", nullable = false)
	private String relativeDirPath;	
	
	// read / write permissions.
	
	@OneToOne(mappedBy="rootDirectoryResource", optional=true)
	@Fetch(FetchMode.JOIN)
	FsResourceStore resourceStore = null;	
	
	/**
	 * 
	 */
	public FsDirectoryResource() {
		
	}
	
	public FsDirectoryResource(Long dirId) {
		setNodeId(dirId);
	}
	
	public FsDirectoryResource(String name, String relativeDirPath) {
		super();
		setName(name);
		this.relativeDirPath = relativeDirPath;
	}
	
	public Long getDirId(){
		return getNodeId();
	}

	/**
	 * @return the relativeDirPath
	 */
	public String getRelativeDirPath() {
		return relativeDirPath;
	}

	/**
	 * @param relativeDirPath the relativeDirPath to set
	 */
	public void setRelativeDirPath(String relativeDirPath) {
		this.relativeDirPath = relativeDirPath;
	}

	/**
	 * @return the resourceStore
	 */
	public FsResourceStore getResourceStore() {
		return resourceStore;
	}

	/**
	 * @param resourceStore the resourceStore to set
	 */
	public void setResourceStore(FsResourceStore resourceStore) {
		this.resourceStore = resourceStore;
	}	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + ", " + FsDirectoryResource.class.getName() + " [relativeDirPath=" + relativeDirPath + "]";
	}

}
