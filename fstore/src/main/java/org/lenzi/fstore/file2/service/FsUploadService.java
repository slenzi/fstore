package org.lenzi.fstore.file2.service;

import java.nio.file.Path;

import org.lenzi.fstore.core.security.FsSecureUser;
import org.lenzi.fstore.core.security.acls.service.FsAclService;
import org.lenzi.fstore.core.security.auth.service.FsAuthenticationService;
import org.lenzi.fstore.core.security.service.FsSecurityService;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.repository.FsFileResourceRepository.FsFileResourceFetch;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use to processes file through the upload pipeline internally (not when users upload
 * files through the upload controller.)
 * 
 * @author sal
 */
@Service
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FsUploadService {

	@InjectLogger
	private Logger logger;
	
    @Autowired
    private FsResourceService fsResourceService; 	
	
    @Autowired
    private FsUploadPipeline uploadPipeline;
    
	//
	// ACLs security
	//
	//@Autowired
	//private FsSecurityService fsSecurityService;
    @Autowired
    private FsAuthenticationService fsAuthService;	    
	
	public FsUploadService() {
		
	}
	
	/**
	 * Uses upload pipeline to rewrite existing text file.
	 * 
	 * @param fileId - id of existing file path resource
	 * @param fileData - new text file data.
	 * @throws ServiceException
	 */
	public void rewriteTextFile(Long fileId, String fileData) throws ServiceException {
		
		//FsSecureUser fsUser = fsSecurityService.getLoggedInUser();
		FsSecureUser fsUser = fsAuthService.getLoggedInUser();
		if(fsUser != null){
			logger.info("User '" + fsUser.getUsername() + "' saving text file, with fileId =>" + fileId);
		}else{
			logger.error("FsSecureUser is null.. cannot save text file, with fileId =? " + fileId);
		}
		
		FsFileMetaResource currentResource = fsResourceService.getFileResourceById(fileId, FsFileResourceFetch.FILE_META);
		String currentFileName = currentResource.getName();
		
		FsDirectoryResource currentDir = fsResourceService.getDirectoryResourceByFileId(fileId);
		Long currentDirId = currentDir.getDirId();
		
		Path tempPath = uploadPipeline.processTextFileToTemp(currentFileName, fileData);
		
		// TODO - might want to block for this to finish. but what it pipeline has a large queue.... you don't want the save operation to take long.
		uploadPipeline.processInternalFiles(fsUser.getFsUser().getUserId(), currentDirId, tempPath, true);
		
	}	

}
