/**
 * 
 */
package org.lenzi.fstore.file2.repository;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.DateUtil;
import org.lenzi.fstore.core.util.FileUtil;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathType;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.service.FsResourceHelper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author sal
 *
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class FsFileResourceStreamAdder extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1756855144789479319L;
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("FsPathResourceTree")
	private TreeRepository<FsPathResource> treeRepository;
	
	@Autowired
	private FsResourceStoreRepository fsResourceStoreRepository;
	
	@Autowired
	private FsDirectoryResourceRepository fsDirectoryResourceRepository;
	
	@Autowired
	private FsFileResourceRepository fsFileResourceRepository;
	
	@Autowired
	private FsResourceHelper fsResourceHelper;

	/**
	 * 
	 */
	public FsFileResourceStreamAdder() {
		
	}
	
	public void test(){
		
		Session session = getEntityManager().unwrap(Session.class);
		
		session.doWork(new Work(){

			@Override
			public void execute(Connection connection) throws SQLException {
				
				// TODO - do something with raw connection
				
			}
			
		});
		
		
	}
	
	/**
	 * Add or replace file
	 * 
	 * @param fileName - name of the file
	 * @param fileBytes - file byte data
	 * @param fsDirId - id of directory where file will be added
	 * @param replaceExisting - true to replace existing file if a file with the same name already exists, false not to replace.
	 * 
	 * @return reference to the newly added file
	 * 
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public FsFileMetaResource addFileResource(String fileName, byte[] fileBytes, Long fsDirId, boolean replaceExisting) throws DatabaseException, IOException {
		
		if(fileName == null){
			throw new DatabaseException("Missing file name. param is null.");
		}else if(fileBytes == null || fileBytes.length == 0){
			throw new DatabaseException("Missing file data. byte array is null or length 0.");
		}
		
		FsDirectoryResource parentDir = fsDirectoryResourceRepository.getDirectoryResourceWithChildren(fsDirId, 1);
		FsFileMetaResource existingFileResource = fsFileResourceRepository.haveExistingFile(fileName, parentDir, false);
		
		FsResourceStore store = null;
		try {
			store = fsResourceStoreRepository.getStoreByPathResourceId(fsDirId);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch resource store for dir id => " + fsDirId, e);
		}
		
		boolean needReplace = existingFileResource != null ? true : false;
		Path absoluteDirPath= fsResourceHelper.getAbsoluteDirectoryPath(store, parentDir);
		
		if(needReplace && !replaceExisting){
			
			throw new DatabaseException("File " + fileName + " already exists in directory " + parentDir.getName() + 
					" at path " + absoluteDirPath + ". Cannot replace existing file because 'replaceExisting' param is false.");
			
		}else if(needReplace && replaceExisting){
			
			// do replace
			return addReplace(fileName, fileBytes, existingFileResource, parentDir, store);
			
		}else{
			
			// do add
			return add(fileName, fileBytes, parentDir, store);
			
		}		
		
	}
	
	/**
	 * Add new file from byte[]
	 * 
	 * @param fileName
	 * @param fileData
	 * @param fsDirectory
	 * @param fsStore
	 * @return
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public FsFileMetaResource add(String fileName, byte[] fileBytes, FsDirectoryResource fsDirectory, FsResourceStore fsStore) throws DatabaseException, IOException {
		
		Path absoluteDirPath	= fsResourceHelper.getAbsoluteDirectoryPath(fsStore, fsDirectory);
		Path absoluteFilePath   = fsResourceHelper.getAbsolutePath(fsStore, fsDirectory, fileName);
		String relativeFilePath = fsResourceHelper.getRelativePath(fsStore, fsDirectory, fileName);	
		
		String contentType = FileUtil.detectMimeType(fileBytes);
		
		logger.info("Adding file => " + fileName + ", size => " + ((fileBytes != null) ? fileBytes.length + " bytes" : "null bytes") +
				", mime type => " + contentType +
				", Directory Id => " + fsDirectory.getDirId() + ", Directory Name => " + fsDirectory.getName() +
				", File system path => " + absoluteDirPath.toString());
		
		// create file entry for meta data
		FsFileMetaResource metaResource = new FsFileMetaResource();
		metaResource.setPathType(FsPathType.FILE);
		metaResource.setStoreId(fsStore.getStoreId());
		metaResource.setName(fileName);
		metaResource.setMimeType(contentType);
		metaResource.setFileSize((long)fileBytes.length); // TODO check this
		metaResource.setRelativePath(relativeFilePath);
		FsFileMetaResource persistedMetaResource = null;
		try {
			persistedMetaResource = (FsFileMetaResource) treeRepository.addChildNode(fsDirectory, metaResource);
		} catch (DatabaseException e) {
			throw new DatabaseException("Error persisting new " + FsFileMetaResource.class.getName() + 
					", file name => " + metaResource.getName() + " to directory " + absoluteDirPath.toString());
		}
		
		// create file entry for byte[] data
		FsFileResource fileResource = new FsFileResource();
		fileResource.setFileId(persistedMetaResource.getFileId());
		fileResource.setFileData(fileBytes);
		persist(fileResource);
		getEntityManager().flush();	
		
		// make sure objects have all data set before returning
		persistedMetaResource.setFileResource(fileResource);
		fileResource.setFileMetaResource(persistedMetaResource);
		
		// write file to disk
		try {
			
			Files.write(absoluteFilePath, fileBytes);
			
		} catch (FileAlreadyExistsException e){
			throw buildDatabaseExceptionWriteError(absoluteFilePath, fsDirectory, e);
		} catch (DirectoryNotEmptyException e){
			throw buildDatabaseExceptionWriteError(absoluteFilePath, fsDirectory, e);
		} catch (IOException e) {
			throw buildDatabaseExceptionWriteError(absoluteFilePath, fsDirectory, e);
		} catch (SecurityException e) {
			throw buildDatabaseExceptionWriteError(absoluteFilePath, fsDirectory, e);
		}
		
		// not really needed... just a little extra precaution
		if(!Files.exists(absoluteFilePath)){
			throw new IOException("Write proceeded without error, but file does not appear to exist in target directory...");
		}
		
		return persistedMetaResource;		
		
	}
	
	/**
	 * 
	 * @param fileName
	 * @param fileData
	 * @param existingFsFileEntry
	 * @param fsDirectory
	 * @param fsStore
	 * @return
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public FsFileMetaResource addReplace(String newFileName, byte[] fileBytes, FsFileMetaResource existingFsFileEntry,
			FsDirectoryResource fsDirectory, FsResourceStore fsStore) throws DatabaseException, IOException {
		
		Long existingFileId = existingFsFileEntry.getFileId();
		String existingFileName = existingFsFileEntry.getName();
		Long existingFileSize = existingFsFileEntry.getFileSize();
		
		Path absoluteDirPath = fsResourceHelper.getAbsoluteDirectoryPath(fsStore, fsDirectory);
		Path existingAbsoluteFilePath = fsResourceHelper.getAbsolutePath(fsStore, fsDirectory, existingFileName);
		Path newAbsoluteFilePath = fsResourceHelper.getAbsolutePath(fsStore, fsDirectory, newFileName);
		String newRelativeFilePath = fsResourceHelper.getRelativePath(fsStore, fsDirectory, newFileName);				
		
		String contentType = FileUtil.detectMimeType(fileBytes);
		
		logger.info("Replacing existing file => " + existingFileName + ", size => " + existingFileSize + " bytes , with new file => " + newFileName +
				", size => " + ((fileBytes != null) ? fileBytes.length + " bytes" : "null bytes") +
				", mime type => " + contentType +
				", Directory Id => " + fsDirectory.getDirId() + ", Directory Name => " + fsDirectory.getName() +
				", File system path => " + absoluteDirPath.toString());
		
		// update database
		FsFileResource updateFileResource = new FsFileResource();
		updateFileResource.setFileId(existingFileId);
		updateFileResource.setFileData(fileBytes);
		existingFsFileEntry.setStoreId(fsStore.getStoreId()); // not really necessary, same store
		existingFsFileEntry.setName(newFileName);
		existingFsFileEntry.setMimeType(contentType);
		existingFsFileEntry.setRelativePath(newRelativeFilePath);
		existingFsFileEntry.setDateUpdated(DateUtil.getCurrentTime());
		existingFsFileEntry.setFileSize((long)fileBytes.length); // TODO - check this
		FsFileResource fsUpdatedFile = (FsFileResource)merge(updateFileResource);
		FsFileMetaResource fsUpdatedMetaFile = (FsFileMetaResource)merge(existingFsFileEntry);
		fsUpdatedMetaFile.setFileResource(fsUpdatedFile);
		fsUpdatedFile.setFileMetaResource(fsUpdatedMetaFile);
		
		// delete old file on disk
		try {
			FileUtil.deletePath(existingAbsoluteFilePath);
		} catch (IOException e) {
			throw new DatabaseException("Could not remove existing file on disk " + existingAbsoluteFilePath.toString());
		}
		
		// write file to disk
		try {
			
			Files.write(newAbsoluteFilePath, fileBytes);
			
		} catch (FileAlreadyExistsException e){
			throw buildDatabaseExceptionWriteError(newAbsoluteFilePath, fsDirectory, e);
		} catch (DirectoryNotEmptyException e){
			throw buildDatabaseExceptionWriteError(newAbsoluteFilePath, fsDirectory, e);
		} catch (IOException e) {
			throw buildDatabaseExceptionWriteError(newAbsoluteFilePath, fsDirectory, e);
		} catch (SecurityException e) {
			throw buildDatabaseExceptionWriteError(newAbsoluteFilePath, fsDirectory, e);
		}		
		
		return fsUpdatedMetaFile;		
		
	}
	
	/**
	 * Builds exception for write error
	 * 
	 * @param target
	 * @param directory
	 * @param e
	 * @return
	 */
	private DatabaseException buildDatabaseExceptionWriteError(Path target, FsDirectoryResource directory, Throwable e){
		
		StringBuffer buf = new StringBuffer();
		String cr = System.getProperty("line.separator");
		buf.append("Error writing target file => " + target.toString() + cr);
		buf.append("Target directory, id => " + directory.getDirId() + ", name => " + directory.getName() + cr);
		buf.append("Throwable => " + e.getClass().getName() + cr);
		buf.append("Message => " + e.getMessage() + cr);
		return new DatabaseException(buf.toString(), e);
		
	}		

}
