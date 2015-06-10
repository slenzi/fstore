/**
 * 
 */
package org.lenzi.fstore.file2.repository.model.impl;

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
	
	// read / write permissions.
	
	@OneToOne(mappedBy="rootDirectoryResource", optional=true)
	@Fetch(FetchMode.JOIN)
	FsResourceStore resourceStore = null;	
	
	public FsDirectoryResource() {
		
		super();
		
		init(null, null, null);
		
	}
	
	public FsDirectoryResource(Long dirId) {
		
		super();
		
		init(dirId, null, null);
		
	}
	
	public FsDirectoryResource(String name, String relativePath) {
		
		super();
		
		init(null, name, relativePath);
		
	}
	
	private void init(Long dirId, String name, String relativePath){
		
		setNodeId(dirId);
		setName(name);
		setRelativePath(relativePath);
		setPathType(FsPathType.DIRECTORY);
		
	}
	
	public Long getDirId(){
		return getNodeId();
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

}
