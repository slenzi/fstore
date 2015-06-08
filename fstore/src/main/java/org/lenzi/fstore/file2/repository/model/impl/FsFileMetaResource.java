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
@DiscriminatorValue("FsFileMetaResource")
@Table(name="FS_FILE_META_RESOURCE")
public class FsFileMetaResource extends FsPathResource {

	/**
	 * 
	 */
	@Transient
	private static final long serialVersionUID = 2220744795886332761L;
	
	@Column(name = "FILE_SIZE", nullable = false)
	private Long fileSize = 0L;

	/**
	 * 
	 */
	public FsFileMetaResource() {

	}

	
}
