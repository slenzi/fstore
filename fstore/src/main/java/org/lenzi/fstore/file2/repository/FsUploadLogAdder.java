/**
 * 
 */
package org.lenzi.fstore.file2.repository;

import java.nio.file.Path;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import org.lenzi.fstore.cms.repository.model.impl.FsCmsSite;
import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.DateUtil;
import org.lenzi.fstore.file2.repository.model.impl.FsUploadLog;
import org.lenzi.fstore.file2.repository.model.impl.FsUploadLogResource;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class FsUploadLogAdder extends AbstractRepository {

	@InjectLogger
	private Logger logger;
		
	
	private static final long serialVersionUID = -3745550013699389177L;

	public FsUploadLogAdder() {

	}
	
	/**
	 * Persist upload log entry
	 * 
	 * @param userId - ID of user who uploaded
	 * @param dirId - ID of directory path resource where files will go
	 * @param tempDir - tempDir holding directory for uploaded files
	 * @param fileMap - map containing uploaded files
	 * @return
	 * @throws DatabaseException
	 */
	public FsUploadLog addLogEntry(Long userId, Long dirId, Path tempDir, Map<String, MultipartFile> fileMap) throws DatabaseException {
		
		FsUploadLog log = new FsUploadLog();
		Set<FsUploadLogResource> resources = new HashSet<FsUploadLogResource>();
		
		fileMap.values().stream().forEach(
			(filePart) -> {
				FsUploadLogResource logRes = new FsUploadLogResource();
				logRes.setResourceName(filePart.getOriginalFilename());
				resources.add(logRes);
			});
		
		log.setUserId(userId);
		log.setTempUploadPath(tempDir.toFile().getAbsolutePath());
		log.setNodeId(dirId);
		log.setDateUploaded(DateUtil.getCurrentTime());
		log.setResources(resources);
		
		try {
			persist(log);
		}catch(DatabaseException e){
			throw new DatabaseException("Error saving upload log entry. ", e);
		}
		
		return log;
		
	}	

}
