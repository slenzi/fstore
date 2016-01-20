package org.lenzi.fstore.file2.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

// used before upgrading to hibernate 5. now we use org.springframework.transaction.annotation.Transactional
//import javax.transaction.Transactional;















import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.CodeTimer;
import org.lenzi.fstore.core.util.FileUtil;
import org.lenzi.fstore.file2.concurrent.service.FsQueuedResourceService;
import org.lenzi.fstore.file2.concurrent.task.AbstractFsTask;
import org.lenzi.fstore.file2.concurrent.task.FsQueuedTaskManager;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.repository.model.impl.FsUploadLog;
import org.lenzi.fstore.file2.web.messaging.UploadMessageService;
import org.lenzi.fstore.main.properties.ManagedProperties;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Processes uploads
 * 
 * @author sal
 */
@Service
@Scope("singleton")
@Transactional
public class FsUploadPipeline {

	@InjectLogger
	private Logger logger;
	
    @Autowired
    private ManagedProperties appProps; 	
	
    @Autowired
    private FsQueuedResourceService fsQueuedResourceService;
    
    @Autowired
    private FsResourceService fsResourceService;    
    
    @Autowired
    private UploadMessageService uploadMessageService;
    
    @Autowired
    private FsResourceHelper fsResourceHelper;
    
    // task manager for adding files to database - adds file meta data, plus 1 byte placeholder for binary data.
	@Autowired
	private FsQueuedTaskManager addFileTaskManager;     
	
    // task manager for binary update process. after file is added we go back and add the binary data to the database
	@Autowired
	private FsQueuedTaskManager updateFileTaskManager;
	
    // task manager for adding upload log entries
	@Autowired
	private FsQueuedTaskManager uploadLogTaskManager;
    
    private String holdingSetupErrorMsg = "";
    
	private FsResourceStore holdingStore = null;
	
	private ExecutorService addFileExecutorService;
	private ExecutorService updateFileExecutorService;
	private ExecutorService uploadLogExecutorService;
	
	private final String TASK_MANAGER_NAME_PREFIX = "Upload Pipeline Task Manager:";
    
	public FsUploadPipeline() {
		
	}
	
	/**
	 * Create holding store, and initialize queued task managers
	 */
	@PostConstruct
	private void init(){
		
		logger.info(FsUploadPipeline.class.getName() + " post construct initialization.");
		
		if(holdingStore == null){
			try {
				holdingStore = getHoldingStore();
			} catch (ServiceException e) {
				holdingSetupErrorMsg = e.getMessage();
				logger.error(e.getMessage());
			}
		}
		
		addFileExecutorService = Executors.newSingleThreadExecutor();
		updateFileExecutorService = Executors.newSingleThreadExecutor();
		uploadLogExecutorService = Executors.newSingleThreadExecutor();
		
		addFileTaskManager.setManagerName(TASK_MANAGER_NAME_PREFIX + " Add File.");
		updateFileTaskManager.setManagerName(TASK_MANAGER_NAME_PREFIX + " Update File Binary Data.");
		uploadLogTaskManager.setManagerName(TASK_MANAGER_NAME_PREFIX + " Upload Log.");
		
		// allows us to get access to the spring security context on other thread pools
		//DelegatingSecurityContextExecutorService addFilesecurityContextExecutor = new DelegatingSecurityContextExecutorService(addFileExecutorService);
		
		addFileTaskManager.startTaskManager(addFileExecutorService);
		//addFileTaskManager.startTaskManager(addFilesecurityContextExecutor);
		
		updateFileTaskManager.startTaskManager(updateFileExecutorService);
		
		uploadLogTaskManager.startTaskManager(uploadLogExecutorService);
		
		logger.info(FsUploadPipeline.class.getName() + " post construct initialization complete!.");
		
	}
	
	/**
	 * Shut down executors and task managers.
	 */
	@PreDestroy
	private void cleanup(){
		
		logger.info(FsUploadPipeline.class.getName() + " pre destroy cleanup.");
		
		addFileTaskManager.stopTaskManager();
		updateFileTaskManager.stopTaskManager();

		if(!addFileExecutorService.isShutdown()){
			logger.error("add file executor service not shutdown...");
		}		
		
		if(!updateFileExecutorService.isShutdown()){
			logger.error("update file executor service not shutdown...");
		}
		
		addFileExecutorService = null;
		updateFileExecutorService = null;
		
		logger.info(FsUploadPipeline.class.getName() + " pre destroy cleanup complete!.");		
		
	}
	
	private Path createTempDir() throws ServiceException {
		
		String parentTempDir = appProps.getProperty("upload.temp.path");
		
		long epochMilli = System.currentTimeMillis();
		String uuid = UUID.randomUUID().toString();
		
		String tempDir = parentTempDir + File.separator + String.valueOf(epochMilli) + "." + uuid;
		
		Path tempPath = Paths.get(tempDir);
		
		try {
			FileUtil.createDirectory(tempPath, true);
		} catch (IOException e) {
			throw new ServiceException("Failed to create temporary directory at " + tempDir + ". " + e.getMessage());
		}		
		
		return tempPath;
		
	}
	
	/**
	 * Write new text file to temp upload dir, for eventual processing through pipeline.
	 * 
	 * @param fileName
	 * @param fileData
	 * @return
	 * @throws ServiceException
	 */
	public Path processTextFileToTemp(String fileName, String fileData) throws ServiceException {
		
		Path tempPath = createTempDir();
		
		Path filePath = Paths.get(tempPath.toFile().getAbsolutePath() + File.separator + fileName);
		
		try {
			Files.write(filePath, Arrays.asList(fileData), java.nio.charset.StandardCharsets.UTF_8, 
					java.nio.file.StandardOpenOption.CREATE);
		} catch (IOException e) {
			throw new ServiceException("Failed to write text file at " + filePath + ". " + e.getMessage());
		}
		
		return tempPath;
		
	}
	
	/**
	 * Save all files in the file map to the temp upload directory.
	 * 
	 * @param fileMap
	 * @return Path to newly created temp directory where all files are saved.
	 * @throws ServiceException
	 */
	private Path processToTemp(Map<String, MultipartFile> fileMap) throws ServiceException {
		
		/*
		String parentTempDir = appProps.getProperty("upload.temp.path");
		
		long epochMilli = System.currentTimeMillis();
		String uuid = UUID.randomUUID().toString();
		
		String tempDir = parentTempDir + File.separator + String.valueOf(epochMilli) + "." + uuid;
		
		Path tempPath = Paths.get(tempDir);
		
		try {
			FileUtil.createDirectory(tempPath, true);
		} catch (IOException e) {
			throw new ServiceException("Failed to create temporary directory for uploaded files, at " + tempDir + ". " + e.getMessage());
		}
		*/
		
		Path tempPath = createTempDir();
		
		// save all files
		fileMap.values().stream().forEach(
			(filePart) -> {
				
				Path filePath = Paths.get(tempPath.toFile().getAbsolutePath() + File.separator + filePart.getOriginalFilename());
				
				try {
					Files.write(filePath, filePart.getBytes());
				} catch (Exception e) {
					throw new RuntimeException("Error saving file " + filePart.getOriginalFilename() + 
							" to directory " + tempPath.toFile().getAbsolutePath() + ". " + e.getMessage(), e);
				}
				
			});
		
		return tempPath;
		
	}
	
	/**
	 * Get the holding resource store. This is where uploads are temporarily stored.
	 * 
	 * @return
	 * @throws ServiceException
	 */
	private FsResourceStore getHoldingStore() throws ServiceException {
		
		String holdingStoreName = appProps.getProperty("resource.store.holding.name");
		
		FsResourceStore holdingStore = null;
		try {
			holdingStore = fsQueuedResourceService.getResourceStoreByName(holdingStoreName);
		} catch (ServiceException e) {
			throw new ServiceException("Failed to fetch holding resource store from database. " + e.getMessage(), e);
		}
		if(holdingStore == null){
			// create store
			String holdingStorePath = appProps.getProperty("resource.store.holding.path");
			String holdingStoreDesc = appProps.getProperty("resource.store.holding.desc");
			try {
				holdingStore = fsQueuedResourceService.addResourceStore(Paths.get(holdingStorePath), holdingStoreName, holdingStoreDesc, true);
			} catch (ServiceException e) {
				throw new ServiceException("Failed to create holding store. " + e.getMessage(), e);
			}
		}
		
		return holdingStore;
		
	}
	
	/**
	 * Process http upload to directory path resource
	 * 
	 * @param userId - id of user who uploaded
	 * @param parentDirId - id of directory path resource where files are being submitted to
	 * @param fileMap - map of files, from upload controller
	 * @param replaceExisting - true to replace any existing files in directory (parentDirId), false not to.
	 * @throws ServiceException
	 */
	public void processUpload(Long userId, Long parentDirId, Map<String, MultipartFile> fileMap, boolean replaceExisting) throws ServiceException {
		
		// save to temp dir
		Path tempDir = null;
		try {
			tempDir = processToTemp(fileMap);
		} catch (ServiceException e) {
			throw new ServiceException("Error saving files to temporary upload directory. ",e);
		}
		
		// process to database directory
		try {
			processToDirectory(userId, tempDir, parentDirId, replaceExisting);
		} catch (ServiceException e) {
			throw new ServiceException("Error processing files to directory with dirId => " + parentDirId, e);
		}
		
	}
	
	/**
	 * Process file from some local directory path
	 * 
	 * @param userId -id of user who is adding the local file
	 * @param parentDirId - id of directory path resource where files are being submitted to
	 * @param dirPath - directory where files are
	 * @param replaceExisting - true to replace any existing files in directory (parentDirId), false not to.
	 * @throws ServiceException
	 */
	public void processInternalFiles(Long userId, Long parentDirId, Path dirPath, boolean replaceExisting) throws ServiceException {
		
		processToDirectory(userId, dirPath, parentDirId, replaceExisting);
		
	}
	
	/**
	 * Processes files to existing directory
	 * 
	 * @param userId -id of user who is adding the file
	 * @param tempDir - temporary directory where uploaded files reside
	 * @param parentDirId - id of parent directory node
	 * @param replaceExisting - true to replace existing files
	 * @throws ServiceException
	 */
	private /*synchronized*/ void processToDirectory(Long userId, Path tempDir, Long parentDirId, boolean replaceExisting) throws ServiceException {
		
		//
		// get paths to all uploaded files
		//
		List<Path> filePaths = null;
		try {
			filePaths = FileUtil.listFilesToDepth(tempDir, 1);
		} catch (IOException e) {
			throw new ServiceException("Error listing files in temporary directory " + tempDir.toString());
		}
		
		//
		// get resource store for the directory where the files will be stored
		//
		FsResourceStore store = null;
		try {
			store = fsResourceService.getResourceStoreByPathResourceId(parentDirId);
		} catch (ServiceException e) {
			throw new ServiceException("Failed to fetch resource store for directory path resource with id => " + 
					parentDirId + ". " + e.getMessage());
		}
		final FsResourceStore finalStore = store;
		
		CodeTimer timer = new CodeTimer();
		
		//
		// Process each file to the database
		//
		filePaths.stream().forEach(
			(pathToFile) ->{
				
				timer.start();
			
				logger.info(">> Process to directory (start) => " + pathToFile);
				
				String fileName = pathToFile.getFileName().toString();
				
				//
				// add file to database
				//
				FsFileMetaResource resource = null;
				try {
					resource = addFileToDirectory(pathToFile, parentDirId, replaceExisting);
				} catch (Exception e) {
					throw new RuntimeException("Error saving file '" + fileName + "' to directory with id '" + parentDirId + "'.", e);
				}
				
				//
				// update database entry with binary data
				//
				try {
					updateWithBinaryData(resource.getFileId(), finalStore);
				} catch (Exception e) {
					throw new RuntimeException("Error updating binary data in database for file '" + fileName + "' in directory with id '" + parentDirId + "'.", e);
				}
				
				//
				// add to upload log
				//
				try {
					addToUploadLog(userId, parentDirId, tempDir, pathToFile);
				} catch (Exception e) {
					throw new RuntimeException("Error adding upload log entry for file '" + fileName + "' in directory with id '" + parentDirId + "'.", e);
				}
						
				timer.stop();
				
				logger.info(">> Process to directory (end) => " + pathToFile + ", elapsed time => " + timer.getElapsedTime());
				
				timer.reset();
				
			});
		
	}
	
	/**
	 * Adds file meta data to database, plus 1-byte placeholder for binary data. Binary data is updated afterwards in
	 * a second database call. We do this for speed reasons. We want all the file meta entries to appear in the database
	 * as fast as possible so the end user sees them in the UI.
	 * 
	 * @param pathToFile
	 * @param parentDirId
	 * @param replaceExisting
	 * @throws ServiceException
	 */
	private FsFileMetaResource addFileToDirectory(Path pathToFile, Long parentDirId, boolean replaceExisting) throws ServiceException {
		
		CodeTimer timer = new CodeTimer();
		
		timer.start();
		
		logger.info(">> addFileToDirectory (start) => " + pathToFile);
		
		AbstractFsTask<FsFileMetaResource> task = getAddToDirectoryTask(pathToFile, parentDirId, replaceExisting);
		
		addFileTaskManager.addTask(task);
		
		FsFileMetaResource resource = task.get(); // block until complete
		
		timer.stop();
		
		logger.info(">> addFileToDirectory (end) => " + pathToFile + ", elapsed time => " + timer.getElapsedTime());

		timer.reset();
		
		return resource;
		
	}
	
	/**
	 * Updates the binary data for the file entry in the database with the file data from disk.
	 * 
	 * @param fileMeta - the database file entry to update
	 * @param store - resource store for the file
	 * @throws ServiceException
	 */
	private void updateWithBinaryData(Long fileId, final FsResourceStore store) throws ServiceException {
		
		CodeTimer timer = new CodeTimer();
		
		timer.start();
		
		logger.info(">> updateWithBinaryData (start) => file id = " + fileId);
		
		AbstractFsTask<Void> task = getUpdateBinaryDataTask(fileId, store);
		
		updateFileTaskManager.addTask(task);
		
		timer.stop();
		
		logger.info(">> updateWithBinaryData (end) => file id = " + fileId + ", elapsed time => " + timer.getElapsedTime());

		timer.reset();		
		
		// do not block
		//task.waitComplete();
		
	}
	
	/**
	 * Adds upload to upload log.
	 * 
	 * @param userId - id of user who uploaded
	 * @param parentDirId - id of directory path resource where uploaded files are being submitted to.
	 * @param tempDir - temp dir where uploaded files reside
	 * @param fileMap - map of uploaded files, from upload controller.
	 * @throws ServiceException
	 */
	private void addToUploadLog(Long userId, Long parentDirId, Path tempDir, Path filePath) throws ServiceException {
		
		CodeTimer timer = new CodeTimer();
		
		timer.start();
		
		logger.info(">> addToUploadLog (start) => user id = " + userId);
		
		AbstractFsTask<FsUploadLog> task = getAddToUploadLogTask(userId, parentDirId, tempDir, filePath);
		
		updateFileTaskManager.addTask(task);
		
		timer.stop();
		
		logger.info(">> addToUploadLog (end) => user id = " + userId + ", elapsed time => " + timer.getElapsedTime());

		timer.reset();	
		
	}
	
	/**
	 * Return a new task which adds/replaces a file in the database (meta data only.)
	 * 
	 * @param pathToFile
	 * @param parentDirId
	 * @param replaceExisting
	 * @return
	 */
	private AbstractFsTask<FsFileMetaResource> getAddToDirectoryTask(Path pathToFile, Long parentDirId, boolean replaceExisting) {
		
		class Task extends AbstractFsTask<FsFileMetaResource> {

			@Override
			public FsFileMetaResource doWork() throws ServiceException {

				String fileName = pathToFile.getFileName().toString();

				FsFileMetaResource resource = fsQueuedResourceService.addFileResourceMeta(pathToFile, parentDirId, replaceExisting);
				
				logger.info("Saved file '" + fileName + "' to directory with id '" + parentDirId + "', Store in DB => " + false);
				
				uploadMessageService.sendUploadProcessedMessage(resource.getFileId(), parentDirId, resource.getName());				
				
				return resource;
				
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};		
		
		return new Task();
		
	}
	
	/**
	 * Return a new task that updates the binary data in the database for the file
	 * 
	 * @param fileId
	 * @param store
	 * @return
	 */
	private AbstractFsTask<Void> getUpdateBinaryDataTask(Long fileId, final FsResourceStore store) {
		
		class Task extends AbstractFsTask<Void> {

			@Override
			public Void doWork() throws ServiceException {
					
				fsQueuedResourceService.syncDatabaseBinary(fileId, store);
					
				return null;

			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		
		return new Task();
		
	}
	
	/**
	 * Return a new task that adds an entry to the upload log.
	 * 
	 * @param userId - id of user who uploaded
	 * @param parentDirId - id of directory path resource where uploaded files are being submitted to.
	 * @param tempDir - temp dir where uploaded files reside
	 * @param filePath - path to file being added
	 * @return
	 */
	private AbstractFsTask<FsUploadLog> getAddToUploadLogTask(Long userId, Long parentDirId, Path tempDir, Path filePath) {
		
		class Task extends AbstractFsTask<FsUploadLog> {

			@Override
			public FsUploadLog doWork() throws ServiceException {

				FsUploadLog log = fsResourceService.logUpload(userId, parentDirId, tempDir, filePath);
				
				logger.info("Saved upload log entry for user with userId => " + userId);			
				
				return log;
				
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};		
		
		return new Task();
		
	}
	
	/**
	 * Creates a new directory in the holding resource store for the uploaded files, then saves all files to the directory.
	 * 
	 * @param tempDir - temporary directory where uploaded files reside
	 * @param replaceExisting - true to replace existing files
	 * @return reference to the newly created directory in the holding resource store.
	 * @throws ServiceException
	 */
	/*
	 * old code
	 * 
	public FsDirectoryResource processToHolding(Path tempDir, boolean replaceExisting) throws ServiceException {
		
		if(holdingStore == null){
			throw new ServiceException("Error, holding resource store is null. Check application logs for error message. " + holdingSetupErrorMsg);
		}
		
		logger.info("Got holding store");
		
		final Long maxAllowedBytesInDb = holdingStore.getMaxFileSizeInDb();		
		
		List<Path> filePaths = null;
		try {
			filePaths = FileUtil.listFilesToDepth(tempDir, 1);
		} catch (IOException e) {
			throw new ServiceException("Error listing files in temporary directory " + tempDir.toString());
		}
		
		LocalDateTime timePoint = LocalDateTime.now();
		
		String dirPostfix = String.format("_%d.%s.%d_%d.%d.%d",
				timePoint.getYear(), timePoint.getMonth(), timePoint.getDayOfMonth(),
				timePoint.getHour(), timePoint.getMinute(), timePoint.getSecond());
		
		String dirName = "Upload" + dirPostfix;
		
		FsDirectoryResource uploadDir = null;
		try {
			uploadDir = fsResourceService.addDirectoryResource(holdingStore.getRootDirectoryResource().getDirId(), dirName);
		} catch (ServiceException e) {
			throw new ServiceException("Error creating new directory for upload in the holding resource store. "
					+ "Directory name = " + dirName + ". " + e.getMessage(),e);
		}
		
		logger.info("Created new directory in holding store for uploaded data: '" + dirName + "'");
		
		final FsDirectoryResource finalDir = uploadDir;
		filePaths.stream().forEach(
			(pathToFile) ->{
			
				addFileExecutor.execute(() -> {
				
					byte[] fileBytes = null;
	
					String fileName = pathToFile.getFileName().toString();
					
					try {
						
						fileBytes = Files.readAllBytes(pathToFile);
						
						boolean storeBinaryInDatabase = fileBytes.length > maxAllowedBytesInDb ? false : true;
						
						FsFileMetaResource resource = fsResourceService.addFileResource(fileName, fileBytes, finalDir.getDirId(), replaceExisting, storeBinaryInDatabase);
						
						logger.info("Saved file '" + fileName + "' to holding store directory '" + dirName + "'. Size bytes => " + 
								fileBytes.length + ", Store in DB => " + storeBinaryInDatabase);
						
						uploadMessageService.sendUploadProcessedMessage(resource.getFileId(), finalDir.getDirId(), resource.getName());
						
					} catch (ServiceException e) {
						throw new RuntimeException("Error saving file '" + fileName + "' to directory '" + dirName + "'.", e);
					} catch (IOException e){
						throw new RuntimeException("IOException thrown when attempting to read file byte data from path. " + e.getMessage(), e);
					}
				
				});
				
			});
		
		return uploadDir;
		
	}
	*/	

}
