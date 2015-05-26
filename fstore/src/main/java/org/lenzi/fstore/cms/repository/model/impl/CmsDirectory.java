/**
 * 
 */
package org.lenzi.fstore.cms.repository.model.impl;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.lenzi.fstore.repository.model.impl.FSNode;
import org.lenzi.fstore.util.DateUtil;

/**
 * Extends FSNode to model a directory in a tree.
 * 
 * @author sal
 */
@Entity
@DiscriminatorValue("CmsDirectory")
@Table(name="FS_CMS_DIRECTORY")
public class CmsDirectory extends FSNode<CmsDirectory> {

	/**
	 * 
	 */
	@Transient
	private static final long serialVersionUID = 213041424219784067L;
	
	// directory name. to get full path you need to get all parent directories, plus the file store root path.
	@Column(name = "DIR_NAME", nullable = false)
	private String dirName;
	
	// link directory to files
	@OneToMany(mappedBy="directory", cascade = CascadeType.PERSIST)
	private Set<CmsFileEntry> fileEntries = new HashSet<CmsFileEntry>(0);
	
	// link directory back to file store. only will have a file store if this is a root directory
	@OneToOne(mappedBy="rootDir", optional=true)
	@Fetch(FetchMode.JOIN)
	CmsFileStore fileStore = null;


	public CmsDirectory(){
		
	}
	
	public CmsDirectory(Long id){
		setNodeId(id);
	}
	
	/**
	 * 
	 */
	public CmsDirectory(String dirName) {
		setName(dirName);
		this.dirName = dirName;
	}

	/**
	 * @return the dirName
	 */
	public String getDirName() {
		return dirName;
	}

	/**
	 * @param dirName the dirName to set
	 */
	public void setDirName(String dirName) {
		this.dirName = dirName;
	}
	
	/**
	 * @return the fileEntries
	 */
	public Set<CmsFileEntry> getFileEntries() {
		return fileEntries;
	}

	/**
	 * @param fileEntries the fileEntries to set
	 */
	public void setFileEntries(Set<CmsFileEntry> fileEntries) {
		this.fileEntries = fileEntries;
	}

	/**
	 * @return the fileStore
	 */
	public CmsFileStore getFileStore() {
		return fileStore;
	}

	/**
	 * @param fileStore the fileStore to set
	 */
	public void setFileStore(CmsFileStore fileStore) {
		this.fileStore = fileStore;
	}
	
	public boolean hasFileStore(){
		return fileStore != null ? true : false;
	}

	public String toString(){
		
		StringBuffer buf = new StringBuffer();
		
		buf.append("[");
		buf.append("name => '" + getName() + "'");
		buf.append(", id => '" + getNodeId() + "'");
		buf.append(", parent_id => '" + getParentNodeId() + "'");
		buf.append(", root => '" + isRootNode() + "'");
		buf.append(", dir_name => '" + getDirName() + "'");
		buf.append(", dt_created => '" + DateUtil.defaultFormat(getDateCreated()) + "'");
		buf.append(", dt_updated => '" + DateUtil.defaultFormat(getDateUpdated()) + "'");
		buf.append("]");
		
		return buf.toString();
		
	}
}
