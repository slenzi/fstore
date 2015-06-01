/**
 * 
 */
package org.lenzi.fstore.cms.repository.model.impl;

import java.util.HashSet;
import java.util.Optional;
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
import org.lenzi.fstore.repository.model.DBClosure;
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
	
	// path relative to store root dir path
	@Column(name = "RELATIVE_DIR_PATH", nullable = false)
	private String relativeDirPath;
	
	// TODO - consider adding STORE_ID so we can easily link back to the CmsFileStore
	
	// link directory to files
	@OneToMany(mappedBy="directory", cascade=CascadeType.ALL)
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

	public CmsDirectory(String dirName, String dirPath) {
		setName(dirName);
		this.dirName = dirName;
		this.relativeDirPath = dirPath;
	}
	
	/**
	 * The directory id is the same as FSNode.getNodeId();
	 * 
	 * @return
	 */
	public Long getDirId(){
		return getNodeId();
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
	 * @return the fileEntries
	 */
	public Set<CmsFileEntry> getFileEntries() {
		return fileEntries;
	}
	
	public boolean hasFileEntries(){
		return (fileEntries != null && fileEntries.size() > 0) ? true : false;
	}

	/**
	 * @param fileEntries the fileEntries to set
	 */
	public void setFileEntries(Set<CmsFileEntry> fileEntries) {
		this.fileEntries = fileEntries;
	}
	
	public void addFileEntry(CmsFileEntry e){
		fileEntries.add(e);
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
	
	/**
	 * Get a child directory of this directory, by name
	 * 
	 * @param dirName - the directory name to match on
	 * @param caseSensitive - pass true for case sensitive match, pass false for case insensitive
	 * @return
	 */
	public CmsDirectory getChildDirectoryByName(String dirName, boolean caseSensitive){
		
		if(!hasChildClosure()){
			return null;
		}
		
		Optional<DBClosure<CmsDirectory>> value = getChildClosure().stream()
				.filter(c -> {
					// only care about first level children
					if(c.getDepth() == 1){
						if(caseSensitive){
							return c.getChildNode().getDirName().equals(dirName);
						}else{
							return c.getChildNode().getDirName().toLowerCase().equals(dirName.toLowerCase());
						}
					}
					return false;
				})
				.findFirst();
			
			return value.isPresent() ? value.get().getChildNode() : null;		
	}
	
	/**
	 * Get the file entry by file name
	 * 
	 * @param fileName - the file name to match on.
	 * @param caseSensitive - pass true for case sensitive match, pass false for case insensitive
	 * @return
	 */
	public CmsFileEntry getEntryByFileName(String fileName, boolean caseSensitive){
		
		if(!hasFileEntries()){
			return null;
		}
		
		Optional<CmsFileEntry> value = fileEntries.stream()
			.filter(e -> {
				if(caseSensitive){
					return e.getFileName().equals(fileName);
				}else{
					return e.getFileName().toLowerCase().equals(fileName.toLowerCase());
				}
			})
			.findFirst();
		
		return value.isPresent() ? value.get() : null;
		
	}
	
	/**
	 * Get file entry by id
	 * 
	 * @param fileId
	 * @return
	 */
	public CmsFileEntry getEntryByFileId(Long fileId){
		
		Optional<CmsFileEntry> value = fileEntries.stream()
			.filter(e -> {
				return e.getFileId().equals(fileId);
			})
			.findFirst();
		
		return value.isPresent() ? value.get() : null;
		
	}
	
	/**
	 * Remove file entry
	 * 
	 * @param fileId
	 * @return the removed entry
	 */
	public CmsFileEntry removeEntryById(Long fileId){
		
		CmsFileEntry removed = null;
		for(CmsFileEntry e : fileEntries){
			if(e.getFileId().equals(fileId)){
				removed = e;
				break;
			}
		}
		fileEntries.remove(removed);
		return removed;
		
	}

	public String toString(){
		
		StringBuffer buf = new StringBuffer();
		
		buf.append("{");
		buf.append("\"name\" : \"" + getName() + "\"");
		buf.append(", \"id\" : \"" + getNodeId() + "\"");
		buf.append(", \"parent_id\" : \"" + getParentNodeId() + "\"");
		buf.append(", \"root\" : \"" + isRootNode() + "\"");
		buf.append(", \"dir_name\" : \"" + getDirName() + "\"");
		buf.append(", \"dir_path\" : \"" + getRelativeDirPath() + "\"");
		buf.append(", \"dt_created\" : \"" + DateUtil.defaultFormat(getDateCreated()) + "\"");
		buf.append(", \"dt_updated\" : \"" + DateUtil.defaultFormat(getDateUpdated()) + "\"");
		buf.append("}");
		
		return buf.toString();
		
	}
}
