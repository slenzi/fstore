/**
 * 
 */
package org.lenzi.fstore.example.repository.model.impl;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
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
@DiscriminatorValue("DirectoryNode")
@Table(name="FS_DIRECTORY_NODE")
public class FSDirectoryNode extends FSNode<FSDirectoryNode> {

	/**
	 * 
	 */
	@Transient
	private static final long serialVersionUID = 213041424219784067L;
	
	@Column(name = "DIR_NAME", nullable = false)
	private String dirName;

	/**
	 * 
	 */
	public FSDirectoryNode(String dirName) {
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
