/**
 * 
 */
package org.lenzi.fstore.cms.repository.model.impl;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.lenzi.fstore.repository.model.impl.FSNode;
import org.lenzi.fstore.util.DateUtil;

/**
 * Extends FSNode to model a directory tree.
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
	
	@Column(name = "DIR_NAME", nullable = false)
	private String dirName;
	
	// link directory to files
	@OneToMany(mappedBy="directory")
	private Set<CmsFileEntry> fileEntries = new HashSet<CmsFileEntry>(0);

	public CmsDirectory(){
		
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
