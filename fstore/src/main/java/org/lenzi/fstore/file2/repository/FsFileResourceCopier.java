/**
 * 
 */
package org.lenzi.fstore.file2.repository;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.FileUtil;
import org.lenzi.fstore.file2.repository.FsFileResourceRepository.FsFileResourceFetch;
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
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FsFileResourceCopier extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5037218061398826797L;

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
	public FsFileResourceCopier() {
		
	}
	
	/**
	 * Copy file, possibly replacing existing file with same name in target directory
	 * 
	 * @param fileId
	 * @param targetDirId
	 * @param replaceExisting
	 * @return
	 * @throws DatabaseException
	 * @throws FileAlreadyExistsException
	 */
	public FsFileMetaResource copyFile(Long fileId, Long targetDirId, boolean replaceExisting) throws DatabaseException, FileAlreadyExistsException {
		
		// get source information
		FsDirectoryResource sourceDir = fsDirectoryResourceRepository.getDirectoryResourceById(targetDirId);
		FsResourceStore sourceStore = fsResourceStoreRepository.getStoreByDirectoryId(sourceDir.getDirId());
		FsFileMetaResource sourceEntry = fsFileResourceRepository.getFileEntry(fileId, FsFileResourceFetch.FILE_META_WITH_DATA);
		
		// get target information
		FsDirectoryResource targetDir = fsDirectoryResourceRepository.getDirectoryResourceById(targetDirId);
		FsResourceStore targetStore = fsResourceStoreRepository.getStoreByDirectoryId(sourceDir.getDirId());
		FsFileMetaResource conflictingTargetEntry = fsFileResourceRepository.haveExistingFile(sourceEntry.getName(), targetDir.getDirId(), false);
			
		// will be true of we need to replace the existing file in the target directory
		boolean needReplace = conflictingTargetEntry != null ? true : false;
		
		// replace existing file in target dir with file from source dir
		if(needReplace && replaceExisting){
			
			return copyReplace(sourceStore, targetStore, sourceDir, targetDir, sourceEntry, conflictingTargetEntry);
			
		// user specified not to replace, throw database exception
		}else if(needReplace && !replaceExisting){
			
			throw new FileAlreadyExistsException("Target directory contains a file with the same name, but 'replaceExisting' param "
					+ "was false. Cannot move file to target directory.");
		
		// simply copy file to target dir
		}else{
			
			return copy(sourceStore, targetStore, sourceDir, targetDir, sourceEntry);
			
		}		
		
	}
	
	/**
	 * Copy file
	 * 
	 * @param sourceStore
	 * @param targetStore
	 * @param sourceDir
	 * @param targetDir
	 * @param sourceEntry
	 * @return
	 * @throws DatabaseException
	 */
	public FsFileMetaResource copy(
			FsResourceStore sourceStore, FsResourceStore targetStore,
			FsDirectoryResource sourceDir, FsDirectoryResource targetDir,
			FsFileMetaResource sourceEntry) throws DatabaseException {
	
		if(sourceEntry.getFileResource() == null){
			throw new DatabaseException("Cannot copy file. " + FsFileMetaResource.class.getName() + " object with id " + 
					sourceEntry.getFileId() + " is missing it's " + FsFileResource.class.getName() + " object. Need this data for copy.");
		}
		
		String newFileName = sourceEntry.getName();
		String relativeFilePath = fsResourceHelper.getRelativePath(targetStore, targetDir, newFileName);
		Path absoluteDirPath	= fsResourceHelper.getAbsoluteDirectoryPath(targetStore, targetDir);
		Path absoluteSourceFilePath = fsResourceHelper.getAbsoluteFilePath(sourceStore, sourceDir, sourceEntry);
		Path absoluteTargetFilePath = fsResourceHelper.getAbsoluteFilePath(targetStore, targetDir, sourceEntry);
		
		logger.info("File copy, source => " + absoluteSourceFilePath.toString() + ", target => " + absoluteTargetFilePath.toString());
		
		// create file entry for meta data
		FsFileMetaResource metaResource = new FsFileMetaResource();
		metaResource.setPathType(FsPathType.FILE);
		metaResource.setName(newFileName);
		metaResource.setFileSize(sourceEntry.getFileSize());
		metaResource.setRelativePath(relativeFilePath);
		FsFileMetaResource persistedMetaResource = null;
		try {
			persistedMetaResource = (FsFileMetaResource) treeRepository.addChildNode(targetDir, metaResource);
		} catch (DatabaseException e) {
			throw new DatabaseException("Error persisting new " + FsFileMetaResource.class.getName() + 
					", file name => " + metaResource.getName() + " to directory " + absoluteDirPath.toString());
		}
		
		// create file entry for byte[] data
		FsFileResource fileResource = new FsFileResource();
		fileResource.setFileId(persistedMetaResource.getFileId());
		fileResource.setFileData(sourceEntry.getFileResource().getFileData());
		persist(fileResource);
		getEntityManager().flush();	
		
		// make sure objects have all data set before returning
		persistedMetaResource.setFileResource(fileResource);
		fileResource.setFileMetaResource(persistedMetaResource);		
		
		// copy physical file on disk
		try {
			
			Files.copy(absoluteSourceFilePath, absoluteTargetFilePath);
			
		} catch (FileAlreadyExistsException e){
			throw buildDatabaseExceptionCopyError(absoluteSourceFilePath, absoluteTargetFilePath, sourceDir, targetDir, e);
		} catch (DirectoryNotEmptyException e){
			throw buildDatabaseExceptionCopyError(absoluteSourceFilePath, absoluteTargetFilePath, sourceDir, targetDir, e);
		} catch (IOException e) {
			throw buildDatabaseExceptionCopyError(absoluteSourceFilePath, absoluteTargetFilePath, sourceDir, targetDir, e);
		} catch (SecurityException e) {
			throw buildDatabaseExceptionCopyError(absoluteSourceFilePath, absoluteTargetFilePath, sourceDir, targetDir, e);
		}
		
		return persistedMetaResource;
		
	}	
	
	/**
	 * Copy file, replacing existing file in target directory with new copy.
	 * 
	 * @param sourceStore
	 * @param targetStore
	 * @param sourceDir
	 * @param targetDir
	 * @param sourceEntry
	 * @param conflictingTargetEntry
	 * @return
	 * @throws DatabaseException
	 */
	public FsFileMetaResource copyReplace(
			FsResourceStore sourceStore, FsResourceStore targetStore,
			FsDirectoryResource sourceDir, FsDirectoryResource targetDir,
			FsFileMetaResource sourceEntry, FsFileMetaResource conflictingTargetEntry) throws DatabaseException {
		
		if(sourceEntry.getFileResource() == null){
			throw new DatabaseException("Cannot copy file. " + FsFileMetaResource.class.getName() + " object with id " + 
					sourceEntry.getFileId() + " is missing it's " + FsFileResource.class.getName() + " object. Need this data for copy.");
		}
		
		String newFileName = sourceEntry.getName();
		String relativeFilePath = fsResourceHelper.getRelativePath(targetStore, targetDir, newFileName);
		Path absoluteDirPath = fsResourceHelper.getAbsoluteDirectoryPath(targetStore, targetDir);
		Path absoluteSourceFilePath = fsResourceHelper.getAbsoluteFilePath(sourceStore, sourceDir, sourceEntry);
		Path absoluteTargetFilePath = fsResourceHelper.getAbsoluteFilePath(targetStore, targetDir, sourceEntry);
		Path absoluteExistingFilePath = fsResourceHelper.getAbsoluteFilePath(targetStore, targetDir, conflictingTargetEntry);
		
		logger.info("File copy-replace, source => " + absoluteSourceFilePath.toString() + ", target (replace) => " + 
				absoluteTargetFilePath + ", existing => " + absoluteExistingFilePath);
		
		// remove existing entry from target dir (cascade delete FsFileResource)
		logger.info("Remove existing file, id => " + conflictingTargetEntry.getFileId());
		treeRepository.removeNode(conflictingTargetEntry);

		// create new file entry for meta data
		FsFileMetaResource metaResource = new FsFileMetaResource();
		metaResource.setPathType(FsPathType.FILE);
		metaResource.setName(newFileName);
		metaResource.setFileSize(sourceEntry.getFileSize());
		metaResource.setRelativePath(relativeFilePath);
		FsFileMetaResource persistedMetaResource = null;
		try {
			persistedMetaResource = (FsFileMetaResource) treeRepository.addChildNode(targetDir, metaResource);
		} catch (DatabaseException e) {
			throw new DatabaseException("Error persisting new " + FsFileMetaResource.class.getName() + 
					", file name => " + metaResource.getName() + " to directory " + absoluteDirPath.toString());
		}
		
		// create file entry for byte[] data
		FsFileResource fileResource = new FsFileResource();
		fileResource.setFileId(persistedMetaResource.getFileId());
		fileResource.setFileData(sourceEntry.getFileResource().getFileData());
		persist(fileResource);
		getEntityManager().flush();			
		
		// make sure objects have all data set before returning
		persistedMetaResource.setFileResource(fileResource);
		fileResource.setFileMetaResource(persistedMetaResource);
		
		// remove conflicting file, then copy over new file
		try {
			
			FileUtil.deletePath(absoluteExistingFilePath);
			
			FileUtil.copyFile(absoluteSourceFilePath, absoluteTargetFilePath);
			
		} catch (FileAlreadyExistsException e){
			throw buildDatabaseExceptionCopyError(absoluteSourceFilePath, absoluteTargetFilePath, sourceDir, targetDir, e);
		} catch (DirectoryNotEmptyException e){
			throw buildDatabaseExceptionCopyError(absoluteSourceFilePath, absoluteTargetFilePath, sourceDir, targetDir, e);
		} catch (IOException e) {
			throw buildDatabaseExceptionCopyError(absoluteSourceFilePath, absoluteTargetFilePath, sourceDir, targetDir, e);
		} catch (SecurityException e) {
			throw buildDatabaseExceptionCopyError(absoluteSourceFilePath, absoluteTargetFilePath, sourceDir, targetDir, e);
		}
		
		return persistedMetaResource;

	}
	
	/**
	 * Used when copying during a traversal (copying directories)
	 * 
	 * @param sourceFileEntryId
	 * @param sourceDirId
	 * @param targetDirId
	 * @param sourceStore
	 * @param targetStore
	 * @param replaceExisting
	 * @return
	 * @throws DatabaseException
	 * @throws FileAlreadyExistsException
	 */
	public FsFileMetaResource copyReplaceTraversal(
			Long sourceFileEntryId, Long sourceDirId, Long targetDirId,
			FsResourceStore sourceStore, FsResourceStore targetStore, boolean replaceExisting) throws DatabaseException, FileAlreadyExistsException {
		
		logger.info("File copy-replace traversal, source file id => " + sourceFileEntryId + ", source dir id => " + 
				sourceDirId + ", target dir id => " + targetDirId + ", replace existing? => " + replaceExisting);
		
		FsFileMetaResource entryToCopy = null;
		FsFileMetaResource existingEntry = null;
		FsDirectoryResource sourceDir = null;
		FsDirectoryResource targetDir = null;
		
		sourceDir = fsDirectoryResourceRepository.getDirectoryResourceById(sourceDirId);
		targetDir = fsDirectoryResourceRepository.getDirectoryResourceById(targetDirId);
		
		entryToCopy = fsFileResourceRepository.getFileEntry(sourceFileEntryId, FsFileResourceFetch.FILE_META_WITH_DATA);
		
		existingEntry = fsFileResourceRepository.haveExistingFile(entryToCopy.getName(), targetDir.getDirId(), false);
		
		// will be true of we need to replace the existing file in the target directory
		boolean needReplace = existingEntry != null ? true : false;
		
		// replace existing file in target dir with file from source dir
		if(needReplace && replaceExisting){
			
			return copyReplace(sourceStore, targetStore, sourceDir, targetDir, entryToCopy, existingEntry);
			
		// user specified not to replace, throw database exception
		}else if(needReplace && !replaceExisting){
			
			throw new FileAlreadyExistsException("Target directory contains a file with the same name, but 'replaceExisting' param "
					+ "was false. Cannot move file to target directory.");
		
		// simply copy file to target dir
		}else{
			
			return copy(sourceStore, targetStore, sourceDir, targetDir, entryToCopy);
			
		}

	}
	
	/**
	 * Builds exception for copy error
	 * 
	 * @param source
	 * @param target
	 * @param sourceDir
	 * @param targetDir
	 * @param e
	 * @return
	 */
	private DatabaseException buildDatabaseExceptionCopyError(Path source, Path target, FsDirectoryResource sourceDir, FsDirectoryResource targetDir, Throwable e){
		
		StringBuffer buf = new StringBuffer();
		String cr = System.getProperty("line.separator");
		buf.append("Error copying file => " + source.toString() + " to target file => " + target.toString() + cr);
		buf.append("Source directory, id => " + sourceDir.getDirId() + ", name => " + sourceDir.getName() + cr);
		buf.append("Target directory, id => " + targetDir.getDirId() + ", name => " + targetDir.getName() + cr);
		buf.append("Throwable => " + e.getClass().getName() + cr);
		buf.append("Message => " + e.getMessage() + cr);
		return new DatabaseException(buf.toString(), e);
		
	}	

}
