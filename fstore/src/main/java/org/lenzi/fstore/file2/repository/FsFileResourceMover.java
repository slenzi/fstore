/**
 * 
 */
package org.lenzi.fstore.file2.repository;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.DateUtil;
import org.lenzi.fstore.core.util.FileUtil;
import org.lenzi.fstore.file2.repository.FsFileResourceRepository.FsFileResourceFetch;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileResource_;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
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
public class FsFileResourceMover extends AbstractRepository {

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
	public FsFileResourceMover() {
		
	}
	
	/**
	 * Move file, possibly replacing existing file with same name in target directory
	 * 
	 * @param fileId
	 * @param targetDirId
	 * @param replaceExisting
	 * @return
	 * @throws DatabaseException
	 * @throws FileAlreadyExistsException
	 */
	public FsFileMetaResource moveFile(Long fileId, Long targetDirId, boolean replaceExisting) throws DatabaseException, FileAlreadyExistsException {
		
		//
		// get source information
		//
		FsDirectoryResource sourceDir = fsDirectoryResourceRepository.getDirectoryResourceByFileId(fileId);
		FsResourceStore sourceStore = fsResourceStoreRepository.getStoreByDirectoryId(sourceDir.getDirId());
		FsFileMetaResource sourceEntry = fsFileResourceRepository.getFileEntry(fileId, FsFileResourceFetch.FILE_META);
		
		//
		// get target information
		//
		//FsDirectoryResource targetDir = fsDirectoryResourceRepository.getDirectoryResourceById(targetDirId);
		FsDirectoryResource targetDir = fsDirectoryResourceRepository.getDirectoryResourceWithChildren(targetDirId, 1);
		FsResourceStore targetStore = fsResourceStoreRepository.getStoreByDirectoryId(targetDir.getDirId());
		//FsFileMetaResource conflictingTargetEntry = fsFileResourceRepository.haveExistingFile(sourceEntry.getName(), targetDir.getDirId(), false);
		FsFileMetaResource conflictingTargetEntry = fsFileResourceRepository.haveExistingFile(sourceEntry.getName(), targetDir, false);
			
		// will be true of we need to replace the existing file in the target directory
		boolean needReplace = conflictingTargetEntry != null ? true : false;
		
		// replace existing file in target dir with file from source dir
		if(needReplace && replaceExisting){
			
			return moveReplace(sourceStore, targetStore, /*sourceDir,*/ targetDir, sourceEntry, conflictingTargetEntry);
			
		// user specified not to replace, throw database exception
		}else if(needReplace && !replaceExisting){
			
			throw new FileAlreadyExistsException("Target directory contains a file with the same name, but 'replaceExisting' param "
					+ "was false. Cannot move file to target directory.");
		
		// simply move file to target dir
		}else{
			
			return move(sourceStore, targetStore, /*sourceDir,*/ targetDir, sourceEntry);
			
		}		
		
	}
	
	/**
	 * Move file
	 * 
	 * @param sourceStore
	 * @param targetStore
	 * @param sourceDir
	 * @param targetDir
	 * @param sourceEntry
	 * @return
	 * @throws DatabaseException
	 */
	public FsFileMetaResource move(
			FsResourceStore sourceStore, FsResourceStore targetStore,
			/*FsDirectoryResource sourceDir,*/ FsDirectoryResource targetDir,
			FsFileMetaResource sourceEntry) throws DatabaseException {
	
		if(sourceEntry.getFileResource() == null){
			throw new DatabaseException("Cannot move file. " + FsFileMetaResource.class.getName() + " object with id " + 
					sourceEntry.getFileId() + " is missing it's " + FsFileResource.class.getName() + " object. Need this data for move.");
		}
		
		String newFileName = sourceEntry.getName();
		String relativeFilePath = fsResourceHelper.getRelativePath(targetStore, targetDir, newFileName);
		Path absoluteSourceFilePath = fsResourceHelper.getAbsoluteFilePath(sourceStore, sourceEntry);
		Path absoluteTargetFilePath = fsResourceHelper.getAbsoluteFilePath(targetStore, targetDir, sourceEntry);
		
		logger.info("File move, source => " + absoluteSourceFilePath.toString() + ", target => " + absoluteTargetFilePath.toString());
		
		// update meta object
		sourceEntry.setRelativePath(relativeFilePath);
		sourceEntry.setDateUpdated(DateUtil.getCurrentTime());
		merge(sourceEntry);
		
		// move tree node
		FsFileMetaResource movedNode = null;
		movedNode = (FsFileMetaResource) treeRepository.moveNode(sourceEntry, targetDir);
		
		// TODO - check data in file meta object..
		
		// move physical file on disk
		try {
			
			FileUtil.moveFile(absoluteSourceFilePath, absoluteTargetFilePath);
			
		} catch (FileAlreadyExistsException e){
			throw buildDatabaseExceptionMoveError(absoluteSourceFilePath, absoluteTargetFilePath, /*sourceDir,*/ targetDir, e);
		} catch (DirectoryNotEmptyException e){
			throw buildDatabaseExceptionMoveError(absoluteSourceFilePath, absoluteTargetFilePath, /*sourceDir,*/ targetDir, e);
		} catch (IOException e) {
			throw buildDatabaseExceptionMoveError(absoluteSourceFilePath, absoluteTargetFilePath, /*sourceDir,*/ targetDir, e);
		} catch (SecurityException e) {
			throw buildDatabaseExceptionMoveError(absoluteSourceFilePath, absoluteTargetFilePath, /*sourceDir,*/ targetDir, e);
		}
		
		return movedNode;
		
	}	
	
	/**
	 * move file, replacing existing file in target directory with copy from source directory
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
	public FsFileMetaResource moveReplace(
			FsResourceStore sourceStore, FsResourceStore targetStore,
			/*FsDirectoryResource sourceDir,*/ FsDirectoryResource targetDir,
			FsFileMetaResource sourceEntry, FsFileMetaResource conflictingTargetEntry) throws DatabaseException {
		
		if(sourceEntry.getFileResource() == null){
			throw new DatabaseException("Cannot move file. " + FsFileMetaResource.class.getName() + " object with id " + 
					sourceEntry.getFileId() + " is missing it's " + FsFileResource.class.getName() + " object. Need this data for move.");
		}
		
		String newFileName = sourceEntry.getName();
		String relativeFilePath = fsResourceHelper.getRelativePath(targetStore, targetDir, newFileName);
		//Path absoluteDirPath = fsResourceHelper.getAbsoluteDirectoryPath(targetStore, targetDir);
		//Path absoluteSourceFilePath = fsResourceHelper.getAbsoluteFilePath(sourceStore, sourceDir, sourceEntry);
		Path absoluteSourceFilePath = fsResourceHelper.getAbsoluteFilePath(sourceStore, sourceEntry);
		Path absoluteTargetFilePath = fsResourceHelper.getAbsoluteFilePath(targetStore, targetDir, sourceEntry);
		Path absoluteExistingFilePath = fsResourceHelper.getAbsoluteFilePath(targetStore, targetDir, conflictingTargetEntry);
		
		logger.info("File move-replace, source => " + absoluteSourceFilePath.toString() + ", target (replace) => " + 
				absoluteTargetFilePath + ", existing => " + absoluteExistingFilePath);
		
		// remove FsFileResource
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaDelete<FsFileResource> cmsFileDelete = cb.createCriteriaDelete(FsFileResource.class);
		Root<FsFileResource> cmsFileRoot = cmsFileDelete.from(FsFileResource.class);
		cmsFileDelete.where(cb.equal(cmsFileRoot.get(FsFileResource_.nodeId), conflictingTargetEntry.getFileId()));
		executeUpdate(cmsFileDelete);
		
		// remove FsFileMetaResource (tree node)
		logger.info("Remove existing file, id => " + conflictingTargetEntry.getFileId());
		treeRepository.removeNode(conflictingTargetEntry);
		
		// update meta object
		sourceEntry.setRelativePath(relativeFilePath);
		sourceEntry.setDateUpdated(DateUtil.getCurrentTime());
		merge(sourceEntry);
		
		// move tree node
		FsFileMetaResource movedNode = null;
		movedNode = (FsFileMetaResource) treeRepository.moveNode(sourceEntry, targetDir);
		
		// TODO - check data in file meta object..
		
		// remove conflicting file, then move over new file
		try {
			
			FileUtil.deletePath(absoluteExistingFilePath);
			
			FileUtil.moveFile(absoluteSourceFilePath, absoluteTargetFilePath);			
			
		} catch (FileAlreadyExistsException e){
			throw buildDatabaseExceptionMoveError(absoluteSourceFilePath, absoluteTargetFilePath, /*sourceDir,*/ targetDir, e);
		} catch (DirectoryNotEmptyException e){
			throw buildDatabaseExceptionMoveError(absoluteSourceFilePath, absoluteTargetFilePath, /*sourceDir,*/ targetDir, e);
		} catch (IOException e) {
			throw buildDatabaseExceptionMoveError(absoluteSourceFilePath, absoluteTargetFilePath, /*sourceDir,*/ targetDir, e);
		} catch (SecurityException e) {
			throw buildDatabaseExceptionMoveError(absoluteSourceFilePath, absoluteTargetFilePath, /*sourceDir,*/ targetDir, e);
		}
		
		return movedNode;

	}
	
	/**
	 * Used when moving files during a traversal (moving directories)
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
	public FsFileMetaResource moveReplaceTraversal(
			Long sourceFileEntryId, /*Long sourceDirId,*/ Long targetDirId,
			FsResourceStore sourceStore, FsResourceStore targetStore, boolean replaceExisting) throws DatabaseException, FileAlreadyExistsException {
		
		//logger.info("File move-replace traversal, source file id => " + sourceFileEntryId + ", source dir id => " + 
		//		sourceDirId + ", target dir id => " + targetDirId + ", replace existing? => " + replaceExisting);
		
		FsFileMetaResource entryToMove = null;
		FsFileMetaResource existingEntry = null;
		//FsDirectoryResource sourceDir = null;
		FsDirectoryResource targetDir = null;
		
		//sourceDir = fsDirectoryResourceRepository.getDirectoryResourceById(sourceDirId);
		//targetDir = fsDirectoryResourceRepository.getDirectoryResourceById(targetDirId);
		targetDir = fsDirectoryResourceRepository.getDirectoryResourceWithChildren(targetDirId, 1);
		
		entryToMove = fsFileResourceRepository.getFileEntry(sourceFileEntryId, FsFileResourceFetch.FILE_META);
		
		//existingEntry = fsFileResourceRepository.haveExistingFile(entryToMove.getName(), targetDir.getDirId(), false);
		existingEntry = fsFileResourceRepository.haveExistingFile(entryToMove.getName(), targetDir, false);
		
		// will be true of we need to replace the existing file in the target directory
		boolean needReplace = existingEntry != null ? true : false;
		
		// replace existing file in target dir with file from source dir
		if(needReplace && replaceExisting){
			
			return moveReplace(sourceStore, targetStore, /*sourceDir,*/ targetDir, entryToMove, existingEntry);
			
		// user specified not to replace, throw database exception
		}else if(needReplace && !replaceExisting){
			
			throw new FileAlreadyExistsException("Target directory contains a file with the same name, but 'replaceExisting' param "
					+ "was false. Cannot move file to target directory.");
		
		// simply move file to target dir
		}else{
			
			return move(sourceStore, targetStore, /*sourceDir,*/ targetDir, entryToMove);
			
		}

	}
	
	/**
	 * Builds exception for move error
	 * 
	 * @param source
	 * @param target
	 * @param sourceDir
	 * @param targetDir
	 * @param e
	 * @return
	 */
	private DatabaseException buildDatabaseExceptionMoveError(Path source, Path target, /*FsDirectoryResource sourceDir,*/ FsDirectoryResource targetDir, Throwable e){
		
		StringBuffer buf = new StringBuffer();
		String cr = System.getProperty("line.separator");
		buf.append("Error moving file => " + source.toString() + " to target file => " + target.toString() + cr);
		//buf.append("Source directory, id => " + sourceDir.getDirId() + ", name => " + sourceDir.getName() + cr);
		buf.append("Target directory, id => " + targetDir.getDirId() + ", name => " + targetDir.getName() + cr);
		buf.append("Throwable => " + e.getClass().getName() + cr);
		buf.append("Message => " + e.getMessage() + cr);
		return new DatabaseException(buf.toString(), e);
		
	}	

}
